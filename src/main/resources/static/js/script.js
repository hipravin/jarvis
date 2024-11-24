const chatbox = document.querySelector(".chatbox");
const chatInput = document.querySelector(".chat-input textarea");
const sendChatBtn = document.querySelector(".chat-input span");
// const codeBlock = document.querySelector(".code-window");
const leftPane = document.querySelector(".left-pane");

let userMessage = null; // Variable to store user's message
const inputInitHeight = chatInput.scrollHeight;

const API_URL = `/api/v1/jarvis/query`;

const createChatLi = (message, className) => {
    // Create a chat <li> element with passed message and className
    const chatLi = document.createElement("li");
    chatLi.classList.add("chat", `${className}`);
    let chatContent = className === "outgoing" ? `<p></p>` : `<span class="material-symbols-outlined">smart_toy</span><p></p>`;
    chatLi.innerHTML = chatContent;
    chatLi.querySelector("p").textContent = message;
    return chatLi; // return chat <li> element
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
const generateResponse = async (chatElement) => {
    const requestOptions = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-XSRF-TOKEN": getCsrfToken()
        },
        body: JSON.stringify({
            query: userMessage
        }),
    }

    const messageElement = chatElement.querySelector("p");
    // Send POST request to API, get response and set the reponse as paragraph text
    try {
        const response = await fetch(API_URL, requestOptions);
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.title + ": " + data.detail);
        }

        // Get the API response text and update the message element
        messageElement.textContent = data.response;
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
    }, 100);
}

const getCsrfToken = () => {
    return document.cookie.replace(/(?:(?:^|.*;\s*)XSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1');
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