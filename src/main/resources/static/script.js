const body = document.body;
const themeBtn = document.getElementById('theme-toggle');
const chatBox = document.getElementById('chat-box');
const form = document.getElementById('chat-form');
const input = document.getElementById('chat-input');
const fileInput = document.getElementById('file-input');
const fileNameSpan = document.getElementById('file-name');
const stopBtn = document.getElementById('stop-btn');
const scrollToBottomBtn = document.getElementById('scroll-to-bottom');
const taskBtn = document.getElementById('task-btn');
const modelSelect = document.getElementById('model-select');

let chatHistory = JSON.parse(localStorage.getItem('chatHistory') || '[]');
let abortCtrl = null;
let editingIdx = null;
let isUserScrolling = false;
let isTaskMode = false;

// Configure marked with highlight.js
marked.setOptions({
  highlight: (code, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value;
    }
    return hljs.highlightAuto(code).value;
  }
});

// Theme initialization
(() => {
  const saved = localStorage.getItem('theme') || 'light';
  body.classList.add(saved + '-theme');
  themeBtn.querySelector('i').classList.toggle('fa-sun', saved === 'dark');
  themeBtn.querySelector('i').classList.toggle('fa-moon', saved === 'light');
})();
themeBtn.onclick = () => {
  const dark = body.classList.toggle('dark-theme');
  body.classList.toggle('light-theme', !dark);
  localStorage.setItem('theme', dark ? 'dark' : 'light');
  themeBtn.querySelector('i').classList.toggle('fa-sun', dark);
  themeBtn.querySelector('i').classList.toggle('fa-moon', !dark);
};

// Task Management toggle
taskBtn.onclick = () => {
  isTaskMode = !isTaskMode;
  taskBtn.classList.toggle('active', isTaskMode);
};

// File input change handler
fileInput.onchange = () => {
  fileNameSpan.textContent = fileInput.files[0] ? fileInput.files[0].name : '';
};

function formatTime(ts) {
  return new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}
function saveHistory() {
  localStorage.setItem('chatHistory', JSON.stringify(chatHistory));
}

// Render messages
function render() {
  chatBox.innerHTML = '';
  if (!chatHistory.length) {
    const msg = document.createElement('div');
    msg.className = 'message bot';
    msg.innerHTML = '<div class="text">What can I help with?</div>';
    chatBox.appendChild(msg);
  } else {
    chatHistory.forEach((m, i) => {
      const msg = document.createElement('div');
      msg.className = `message ${m.role}`;
      msg.innerHTML = `<div class="text">${marked.parse(m.content)}</div>
                       <span class="timestamp">${formatTime(m.timestamp)}</span>`;
      const ctrls = document.createElement('div');
      ctrls.className = 'controls';

      const copyBtn = document.createElement('button');
      copyBtn.innerHTML = '<i class="fas fa-copy"></i>';
      copyBtn.title = 'Copy';
      copyBtn.setAttribute('aria-label', 'Copy message');
      copyBtn.onclick = () => {
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(m.content).catch(() => {});
        } else {
          const ta = document.createElement('textarea');
          ta.value = m.content;
          document.body.appendChild(ta);
          ta.select();
          document.execCommand('copy');
          document.body.removeChild(ta);
        }
      };
      ctrls.appendChild(copyBtn);

      if (m.role === 'user') {
        const editBtn = document.createElement('button');
        editBtn.innerHTML = '<i class="fas fa-edit"></i>';
        editBtn.title = 'Edit';
        editBtn.setAttribute('aria-label', 'Edit message');
        editBtn.onclick = () => {
          input.value = m.content;
          input.focus();
          editingIdx = i;
        };
        ctrls.appendChild(editBtn);
      }

      msg.appendChild(ctrls);
      chatBox.appendChild(msg);
    });
  }
  if (!isUserScrolling) {
    chatBox.scrollTop = chatBox.scrollHeight;
  }
}

// Detect user scrolling
chatBox.addEventListener('scroll', () => {
  const isAtBottom = chatBox.scrollHeight - chatBox.scrollTop <= chatBox.clientHeight + 1;
  isUserScrolling = !isAtBottom;
  scrollToBottomBtn.style.display = isUserScrolling ? 'block' : 'none';
});

// Scroll to bottom button
scrollToBottomBtn.onclick = () => {
  chatBox.scrollTop = chatBox.scrollHeight;
  isUserScrolling = false;
  scrollToBottomBtn.style.display = 'none';
};

// Enter=submit, Shift+Enter=newline
input.addEventListener('keydown', e => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    form.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
  }
});

// Submit handler
form.addEventListener('submit', async e => {
  e.preventDefault();
  const txt = input.value.trim();
  if (!txt) return;

  if (editingIdx != null) {
    chatHistory[editingIdx].content = txt;
    chatHistory[editingIdx].timestamp = new Date().toISOString();
    chatHistory = chatHistory.slice(0, editingIdx + 1);
    editingIdx = null;
  } else {
    chatHistory.push({ role: 'user', content: txt, timestamp: new Date().toISOString() });
  }
  saveHistory();
  render();
  input.value = '';

  stopBtn.style.display = '';
  abortCtrl = new AbortController();

  const typingIndicator = document.createElement('div');
  typingIndicator.className = 'typing-indicator';
  typingIndicator.textContent = '';
  chatBox.appendChild(typingIndicator);
  if (!isUserScrolling) {
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  const botMsgDiv = document.createElement('div');
  botMsgDiv.className = 'message bot';
  botMsgDiv.innerHTML = '<div class="text"></div>';
  chatBox.appendChild(botMsgDiv);
  if (!isUserScrolling) {
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  try {
    const formData = new FormData();
    formData.append('query', txt);
    formData.append('mode', isTaskMode ? 'todo' : 'chat');
    formData.append('model', modelSelect.value);
    if (fileInput.files[0]) {
      formData.append('file', fileInput.files[0]);
    }

    const endpoint = fileInput.files[0] ? '/api/v2/chat/media' : '/api/v2/chat';
    const res = await fetch(`http://192.168.29.204:9990${endpoint}`, {
      method: 'POST',
      body: formData,
      signal: abortCtrl.signal
    });

    if (!res.ok || !res.body) throw new Error('Request failed');
    const reader = res.body.getReader();
    const dec = new TextDecoder();
    let buffer = '';
    const textDiv = botMsgDiv.querySelector('.text');

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += dec.decode(value, { stream: true });
      textDiv.innerHTML = marked.parse(buffer);
      if (!isUserScrolling) {
        chatBox.scrollTop = chatBox.scrollHeight;
      }
    }

    if (chatBox.contains(typingIndicator)) {
      chatBox.removeChild(typingIndicator);
    }
    stopBtn.style.display = 'none';
    chatHistory.push({ role: 'bot', content: buffer, timestamp: new Date().toISOString() });
    fileInput.value = '';
    fileNameSpan.textContent = '';
    saveHistory();
    render();
  } catch (err) {
    stopBtn.style.display = 'none';
    if (chatBox.contains(typingIndicator)) {
      chatBox.removeChild(typingIndicator);
    }
    const textDiv = botMsgDiv.querySelector('.text');
    textDiv.textContent = err.name === 'AbortError' ? '*Response stopped.*' : 'Error processing request.';
    chatHistory.push({ role: 'bot', content: textDiv.textContent, timestamp: new Date().toISOString() });
    fileInput.value = '';
    fileNameSpan.textContent = '';
    saveHistory();
    render();
  }
});

// Stop button
stopBtn.onclick = () => {
  if (abortCtrl) abortCtrl.abort();
  stopBtn.style.display = 'none';
};

// Download chat history as text
document.getElementById('download-btn').onclick = () => {
  const data = chatHistory
    .map(m => `[${formatTime(m.timestamp)}] ${m.role.toUpperCase()}: ${m.content}`)
    .join('\n');
  const blob = new Blob([data], { type: 'text/plain' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = 'chat.txt';
  a.click();
  URL.revokeObjectURL(a.href);
};

// Clear history
document.getElementById('clear-btn').onclick = () => {
  localStorage.clear();
  chatHistory = [];
  fileInput.value = '';
  fileNameSpan.textContent = '';
  render();
};

// Initial render
render();