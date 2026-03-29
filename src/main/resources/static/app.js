const chatbox = document.querySelector(".chatbox");
const chatInput = document.querySelector(".chat-input textarea");
const sendChatBtn = document.querySelector(".chat-input span");
// const codeBlock = document.querySelector(".code-window");
const leftPane = document.querySelector(".left-pane");
const ghToggle = document.getElementById("gh-toggle");
const gbToggle = document.getElementById("gb-toggle");
const bsToggle = document.getElementById("bs-toggle");
const seToggle = document.getElementById("se-toggle");
const loggedInLabel = document.getElementById("logged-in-label");

let userMessage = null; // Variable to store user's message
let lastSearchResponseBody = null;

const inputInitHeight = chatInput.scrollHeight;

const BASE_API_URL = '';
const SEARCH_URL = `${BASE_API_URL}/api/search`;
const USER_INFO_URL = `${BASE_API_URL}/api/user/info`;

const createChatLi = (message, className) => {
    // Create a chat <li> element with passed message and className
    const chatLi = document.createElement("li");
    chatLi.classList.add("chat", `${className}`);
    //TODO: ul cannot be inside p, rework it
    let chatContent = className === "outgoing" ? `<p></p>` : `<span class="material-symbols-outlined">smart_toy</span><p></p><ul></ul>`;
    chatLi.innerHTML = chatContent;
    chatLi.querySelector("p").textContent = message;
    return chatLi; // return chat <li> element
}

const fillItems = (items, chatLi) => {
    let responseItemsUl = chatLi.querySelector("ul");
    if (!responseItemsUl) {
        responseItemsUl = document.createElement("ul");
        chatLi.appendChild(responseItemsUl);
    }
    responseItemsUl.className = "authors-ul";

    items.forEach((item) => {
        const li = document.createElement("li");
        li.innerHTML =
            `<div class="response-item"><a href="${item.title.url}" target="_blank"></a><p></p></div>`;

        const searchSourceIcon = document.createElement("span");
        searchSourceIcon.classList.add("search-source-icon");
        if (item.source === "GITHUB") {
            searchSourceIcon.classList.add("material-symbols-outlined");
            searchSourceIcon.innerHTML = '<svg viewBox="0 0 24 24" fill="currentColor" width="25" height="25"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/></svg>';
        } else if (item.source === "BOOKSTORE") {
            searchSourceIcon.classList.add("material-symbols-outlined");
            searchSourceIcon.textContent = "import_contacts";
        } else if (item.source === "GOOGLE_BOOKS") {
            searchSourceIcon.classList.add("material-symbols-outlined");
            searchSourceIcon.textContent = "book_2";
        } else if (item.source === "STACKEXCHANGE") {
            searchSourceIcon.classList.add("material-symbols-outlined");
            searchSourceIcon.textContent = "stacks";
        }
        li.querySelector(".response-item").prepend(searchSourceIcon);

        li.querySelector(".response-item >a").textContent = item.title.title;
        // li.querySelector(".response-item >p").textContent = item.shortDescription;
        li.querySelector(".response-item >p").innerHTML = item.main.text;
        responseItemsUl.appendChild(li);
    });
}

const enabledSearchProviders = () => {
    const providers = [];
    if (ghToggle.classList.contains("provider-on")) {
        providers.push("GITHUB");
    }
    if (bsToggle.classList.contains("provider-on")) {
        providers.push("BOOKSTORE");
    }
    if (seToggle.classList.contains("provider-on")) {
        providers.push("STACKEXCHANGE");
    }
    if (gbToggle.classList.contains("provider-on")) {
        providers.push("GOOGLE_BOOKS");
    }

    return providers;
}

const generateResponse = async (chatElement) => {
    const requestOptions = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-XSRF-TOKEN": getCsrfToken()
        },
        body: JSON.stringify({
            "query": userMessage,
            "sources": enabledSearchProviders()
        }),
    }

    const scrollHeightBefore = chatbox.scrollHeight;
    console.log(`scroll height before: ${scrollHeightBefore}`);
    const messageElement = chatElement.querySelector("p");

    try {
        const response = await fetch(SEARCH_URL, requestOptions);
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.title + ": " + data.detail);
        }

        messageElement.textContent = "";
        messageElement.textContent = data.response;
        if (data.excerpts) {
            fillItems(data.excerpts, messageElement)
        }
    } catch (error) {
        // Handle error
        messageElement.classList.add("error");
        messageElement.textContent = error.message;
    } finally {
        // chatbox.scrollTo(0, chatbox.scrollHeight);
        chatbox.scrollTo(0, scrollHeightBefore - chatbox.offsetHeight + 100);//small constant scroll advance from previous for interactiveness
    }
}

const requesUserInfo = async () => {
    let response = await fetch(USER_INFO_URL);
    let user = await response.json();

    loggedInLabel.textContent = `Hello, ${user?.username}`;
    return user;
}

const handleChat = () => {
    userMessage = chatInput.value.trim(); // Get user entered message and remove extra whitespace
    if (!userMessage) return;

    // Clear the input textarea and set its height to default
    chatInput.value = "";
    chatInput.style.height = `${inputInitHeight}px`;

    // Append the user's message to the chatbox
    chatbox.appendChild(createChatLi(userMessage, "outgoing"));
    chatbox.scrollTo(0, chatbox.scrollHeight);

    setTimeout(() => {
        // Display "Thinking..." message while waiting for the response
        const incomingChatLi = createChatLi("Thinking...", "incoming");
        chatbox.appendChild(incomingChatLi);
        chatbox.scrollTo(0, chatbox.scrollHeight);
        generateResponse(incomingChatLi);
    }, 1000);
}

const getCsrfToken = () => {
    return document.cookie.replace(/(?:(?:^|.*;\s*)XSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1');
}

const toggleClass = (elem, classValueOn, classValueOff) => {
    if (elem.classList.contains(classValueOn)) {
        elem.classList.remove(classValueOn);
        elem.classList.add(classValueOff);
    } else if (elem.classList.contains(classValueOff)) {
        elem.classList.remove(classValueOff);
        elem.classList.add(classValueOn);
    }
}

chatInput.addEventListener("input", () => {
    // Adjust the height of the input textarea based on its content
    chatInput.style.height = `${inputInitHeight}px`;
    chatInput.style.height = `${chatInput.scrollHeight}px`;
});

chatInput.addEventListener("keydown", (e) => {
    // If Enter key is pressed without Shift key and the window
    // width is greater than 800px, handle the chat
    if (e.key === "Enter" && !e.shiftKey && window.innerWidth > 800) {
        e.preventDefault();
        handleChat();
    }
});
document.addEventListener('DOMContentLoaded', (event) => {
    requesUserInfo();
});
// window.onload(() => {
//     requestLoggedIn();
// });

sendChatBtn.addEventListener("click", handleChat);
ghToggle.addEventListener("click", (e) => {
    toggleClass(ghToggle, "provider-on", "provider-off");
});
bsToggle.addEventListener("click", (e) => {
    toggleClass(bsToggle, "provider-on", "provider-off");
});
gbToggle.addEventListener("click", (e) => {
    toggleClass(gbToggle, "provider-on", "provider-off");
});
seToggle.addEventListener("click", (e) => {
    toggleClass(seToggle, "provider-on", "provider-off");
});
