//page elements
const loginForm = document.querySelector("#input-form");
const msgForm = document.querySelector("#msg-form");
const chatContainerWithHeader = document.querySelector("#chat-container-with-header");
const loginBox = document.querySelector("#login-box");
const onlineUsers = document.querySelector("#users");
const inputMsg = document.querySelector("#input-msg");
const sendMessageButton = document.querySelector("#send-msg-btn");
const loginButton = document.querySelector("#send-username-btn");
const inputUsername = document.querySelector("#input-username");
const messages = document.querySelector("#messages");
const chatWith = document.querySelector("#chat-with");
const publicChatBtn = document.querySelector("#public-chat-btn");

//string literals
const PUBLIC_CHAT_CONTENT = 'Public chat'

//context
let currentUser = null;
let stompClient = null;

//event listeners of login box
loginButton.addEventListener("click", connect);
loginForm.addEventListener("submit", function (event) {
    event.preventDefault();
    connect().then(() => console.log("Successfully logged in"));
});

//event listeners of chat box
sendMessageButton.addEventListener("click", sendMessage);
msgForm.addEventListener("submit", function (event) {
    event.preventDefault();
    sendMessage();
});
publicChatBtn.addEventListener('click', function (event) {
    event.preventDefault();
    switchToPublicChat().then(() => console.log("Switched to public chat"));
});

//ui builders
function displayPopup(message) {
    const popup = document.createElement("div");
    popup.textContent = message;
    popup.classList.add("popup-visible");
    loginBox.appendChild(popup);
    setTimeout(() => {
        popup.remove();
    }, 3000);
}

function showChatBox() {
    loginBox.classList.add("hidden");
    chatContainerWithHeader.classList.remove("hidden");
}

function createNewChatMessage(message) {
    const newMessageBox = buildMessageElement(message);
    messages.appendChild(newMessageBox);
    newMessageBox.scrollIntoView({behavior: "smooth"});
}

function buildMessageElement(message) {
    const newMessageBox = document.createElement("div");
    newMessageBox.classList.add("message-container");

    const messageHeader = document.createElement("div");
    messageHeader.classList.add("message-header");

    const author = document.createElement("div");
    author.classList.add("sender");
    author.textContent = message.sender;

    const timestamp = document.createElement("div");
    timestamp.classList.add("date");
    timestamp.textContent = (new Date(message.timestamp)).toLocaleString("it-IT");

    messageHeader.append(author, timestamp);
    newMessageBox.append(messageHeader);

    const messageContent = document.createElement("div");
    messageContent.classList.add("message");
    messageContent.textContent = message.content;

    newMessageBox.appendChild(messageContent);
    return newMessageBox;
}

function refreshOnlineUsers(users) {
    onlineUsers.replaceChildren();
    users.forEach(user => {
        const userNode = document.getElementById(user.username)
        if (!userNode && user.username !== currentUser) createNewOnlineUser(user.username);
    })
}

function createNewOnlineUser(username) {
    const onlineUser = document.createElement("button");
    onlineUser.classList.add("chat-button");
    onlineUser.type = 'button'
    onlineUser.id = username + '_container';
    onlineUser.classList.add("user-container");
    onlineUser.addEventListener('click', () => switchToPrivateChat(username))
    const user = document.createElement("div");
    user.id = username;
    user.textContent = username;
    user.classList.add('user')
    onlineUser.append(user);
    onlineUsers.append(onlineUser);
}

//stomp functions
function sendMessage() {
    const messageContent = inputMsg.value.trim();
    if (messageContent && stompClient) {
        const chatMessage = {
            sender: currentUser,
            content: messageContent,
            type: 'CHAT',
            timestamp: new Date()
        };
        if (chatWith.innerText === PUBLIC_CHAT_CONTENT) {
            chatMessage.private = false;
        } else {
            chatMessage.private = true;
            chatMessage.receiver = chatWith.innerText;
        }
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        inputMsg.value = "";
    }
}

//rest functions
async function switchToPrivateChat(receiver) {
    if (chatWith.innerText === receiver) {
        console.log('This chat is already with ' + receiver)
    } else {
        await fetch(`/api/messages/get-private-chat?sender=${encodeURIComponent(currentUser)}&receiver=${encodeURIComponent(receiver)}`)
            .then(response => {
                if (!response.ok) throw new Error('Failed to load chat history');
                chatWith.innerText = receiver;
                messages.replaceChildren();
                const counter = document.getElementById(receiver + '_counter');
                if (counter) counter.remove();
                return response.text().then(text => text ? JSON.parse(text) : []);
            })
            .then(data => {
                data.forEach(createNewChatMessage);
            })
            .catch(error => {
                console.error('Error loading chat history:', error);
            });
    }
}

async function switchToPublicChat() {
    if (chatWith.innerText === PUBLIC_CHAT_CONTENT) {
        console.log('This is already the public chat')
    } else {
        await fetch("/api/messages/get-public-chat")
            .then(response => {
                if (!response.ok) throw new Error('Failed to load chat history');
                chatWith.innerText = PUBLIC_CHAT_CONTENT
                messages.replaceChildren();
                return response.text().then(text => text ? JSON.parse(text) : []);
            })
            .then(data => {
                data.forEach(createNewChatMessage);
            })
            .catch(error => console.error(error));
    }
}

async function connect() {
    try {
        currentUser = inputUsername.value.trim();
        if (!currentUser) {
            displayPopup("Please enter a username.");
            return;
        }
        const validation = await validateUser(currentUser);
        if (validation.isValid) {
            setupWebSocket();
        } else {
            displayPopup(validation.error)
        }
    } catch (error) {
        console.error("An error occurred during the connection process:", error);
        displayPopup("Connection error. Please try again.");
    }
}

async function validateUser(user) {
    try {
        const response = await fetch(`/api/users/validate-username?username=${encodeURIComponent(user)}`);
        if (!response.ok) {
            const errorText = await response.text();
            return {
                isValid: false,
                error: errorText
            }
        }
        return {isValid: true}
    } catch (error) {
        return {
            isValid: false,
            error: error.message
        }
    }
}

function setupWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, error => console.error(error));
}

function onConnected() {
    switchToPublicChat().then(() => {
        stompClient.subscribe('/topic/public', onMessageReceived);
        sendJoinNotification();
        loadOnlineUsers();
        showChatBox();
    });
}

function sendJoinNotification() {
    const joinMessage = {
        sender: currentUser,
        type: "JOIN",
        timestamp: new Date()
    }
    stompClient.send("/app/chat.addUser", {}, JSON.stringify(joinMessage));
}

function loadOnlineUsers() {
    fetch("/api/users/online-users")
        .then(response => {
            if (!response.ok) throw new Error('Failed to load online users');
            return response.text().then(text => text ? JSON.parse(text) : []);
        })
        .then(refreshOnlineUsers)
        .catch(error => {
            console.error('Error loading online users:', error);
        });
}

function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    switch (message.type) {
        case "CHAT": {
            const publicMsg_whilePublicChat = !message.private && chatWith.innerText === PUBLIC_CHAT_CONTENT;
            const privateMsg_byCurrUser = message.private && currentUser === message.sender;
            const privateMsg_toCurrUser = message.private && currentUser === message.receiver;
            const privateMsg_toCurrChatUser = message.private && chatWith.innerText === message.receiver;
            const privateMsg_byCurrChatUser = message.private && chatWith.innerText === message.sender;
            const privateMsg_betweenCurrentChatter = (privateMsg_byCurrChatUser && privateMsg_toCurrUser) || (privateMsg_byCurrUser && privateMsg_toCurrChatUser);
            const privateMsg_toCurrUser_fromOtherPrivateChat = message.private && privateMsg_toCurrUser && !privateMsg_betweenCurrentChatter
            const currentChatMsg = publicMsg_whilePublicChat || privateMsg_betweenCurrentChatter;

            if (currentChatMsg) {
                createNewChatMessage(message);
                if (privateMsg_betweenCurrentChatter) moveChatButtonToTop(message.receiver);
            } else if (privateMsg_toCurrUser_fromOtherPrivateChat) {
                moveChatButtonToTop(message.sender);
                let counter = document.getElementById(message.sender + '_counter');
                if (!counter) {
                    counter = initNewMessageCounter(message.sender);
                }
                incrementMessageCounter(counter);
            }

            break;
        }
        case "JOIN": {
            loadOnlineUsers();
            break;
        }
        case 'LEAVE': {
            document.getElementById(message.sender + '_container').remove();
            break;
        }
    }
}

function moveChatButtonToTop(user) {
    const userContainer = document.getElementById(user + '_container');
    const onlineUsers = document.getElementById('users');
    onlineUsers.removeChild(userContainer);
    onlineUsers.prepend(userContainer);
}

function initNewMessageCounter(user) {
    const counter = document.createElement("div");
    counter.textContent = '0';
    counter.id = user + '_counter';
    counter.classList.add('new-message-counter')
    const userContainer = document.getElementById(user + '_container');
    userContainer.append(counter);
    return counter;
}

function incrementMessageCounter(counter) {
    const content = counter.textContent.trim();
    const number = parseInt(content, 10);
    if (!isNaN(number)) {
        counter.textContent = (number + 1).toString();
        counter.classList.remove('hidden');
    }
}