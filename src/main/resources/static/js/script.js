const chatbox = document.querySelector(".chatbox");
const chatInput = document.querySelector(".chat-input textarea");
const sendChatBtn = document.querySelector(".chat-input span");
// const codeBlock = document.querySelector(".code-window");
const leftPane = document.querySelector(".left-pane");
const ghToggle = document.getElementById("gh-toggle");
const gbToggle = document.getElementById("gb-toggle");

let userMessage = null; // Variable to store user's message
let lastSearchResponseBody = null;

const inputInitHeight = chatInput.scrollHeight;

const API_URL = `/api/v1/jarvis/query`;

const createChatLi = (message, className) => {
    // Create a chat <li> element with passed message and className
    const chatLi = document.createElement("li");
    chatLi.classList.add("chat", `${className}`);
    //TODO: ul cannot be inside p, rework it
    let chatContent = className === "outgoing" ? `<p></p>` : `<span class="material-symbols-outlined">smart_toy</span><p><ul></ul></p>`;
    chatLi.innerHTML = chatContent;
    chatLi.querySelector("p").textContent = message;
    return chatLi; // return chat <li> element
}

const fillItems = (items, chatLi) => {
    let responseItemsUl = chatLi.querySelector("ul");
    if(!responseItemsUl) {
        responseItemsUl = document.createElement("ul");
        chatLi.appendChild(responseItemsUl);
    }
    responseItemsUl.className = "authors-ul";

    items.forEach((item) => {
        const li = document.createElement("li");
        li.innerHTML =
            `<div class="response-item"><a href="${item.header.href}" target="_blank"></a><p></p></div>`;

        const searchSourceIcon = document.createElement("span");
        if(item.searchProvider === "GITHUB") {
            searchSourceIcon.classList.add("material-symbols-outlined");
            searchSourceIcon.textContent = "code_blocks";
        } else if(item.searchProvider === "GOOGLE_BOOKS") {
            searchSourceIcon.classList.add("material-symbols-outlined");
            searchSourceIcon.textContent = "book_2";
        }
        li.querySelector(".response-item").prepend(searchSourceIcon);

        li.querySelector(".response-item >a").textContent = item.header.title;
        // li.querySelector(".response-item >p").textContent = item.shortDescription;
        li.querySelector(".response-item >p").innerHTML = item.shortDescription;
        responseItemsUl.appendChild(li);
    });
}

const fillAuthors = (authors, chatLi) => {
    let chatAuthorsUl = chatLi.querySelector("ul");
    if(!chatAuthorsUl) {
        chatAuthorsUl = document.createElement("ul");
        chatLi.appendChild(chatAuthorsUl);
    }
    chatAuthorsUl.className = "authors-ul";

    authors.forEach((author) => {
        const li = document.createElement("li");
        li.innerHTML = `<a href="#">${author.author + ": " + author.count}</a>`;
        chatAuthorsUl.appendChild(li);
    });
}

const createLink = (title, href) => {
    let a = document.createElement('a');
    let link = document.createTextNode(title);
    a.appendChild(link);
    a.title = title;
    a.href = href;
    a.target = "_blank";

    return a;
}

const createCodeBlock = (block) => {
    let div = document.createElement('div');
    let p = document.createElement('p');
    let link = createLink(block.link.title, block.link.href);
    div.classList.add("code-window", "multiline");
    p.textContent = block.code;
    div.appendChild(link);
    div.appendChild(p);

    return div;
}

const fillLeftPaneWithCode = (codeSearchResponse) => {
    leftPane.innerHTML = "";

    codeSearchResponse.code_fragments.forEach(fragment => {
        leftPane.appendChild(createCodeBlock(fragment));
    });
}
const enabledSearchProviders = () => {
    const providers = [];
    if(ghToggle.classList.contains("provider-on")) {
        providers.push("GITHUB");
    }
    if(gbToggle.classList.contains("provider-on")) {
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
            "providers": enabledSearchProviders()
        }),
    }

    const messageElement = chatElement.querySelector("p");

    try {
        const response = await fetch(API_URL, requestOptions);
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.title + ": " + data.detail);
        }

        messageElement.textContent = "";
        messageElement.textContent = data.response;
        if(data.items) {
            fillItems(data.items, messageElement)
        }

        fillLeftPaneWithCode(data);
    } catch (error) {
        // Handle error
        messageElement.classList.add("error");
        messageElement.textContent = error.message;
    } finally {
        chatbox.scrollTo(0, chatbox.scrollHeight);
    }
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
    if(elem.classList.contains(classValueOn)) {
        elem.classList.remove(classValueOn);
        elem.classList.add(classValueOff);
    } else if(elem.classList.contains(classValueOff)) {
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

sendChatBtn.addEventListener("click", handleChat);
ghToggle.addEventListener("click", (e)=> {
    toggleClass(ghToggle, "provider-on", "provider-off");
});
gbToggle.addEventListener("click", (e)=> {
    toggleClass(gbToggle, "provider-on", "provider-off");
});