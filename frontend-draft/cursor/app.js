/**
 * Jarvis — Frontend only. Sends requests to server and displays response history.
 */

(function () {
  const messagesEl = document.getElementById('messages');
  const inputForm = document.getElementById('inputForm');
  const queryInput = document.getElementById('queryInput');
  const btnSend = document.getElementById('btnSend');
  const btnNewChat = document.getElementById('btnNewChat');
  const welcomeScreen = document.getElementById('welcomeScreen');
  const chatHistory = document.getElementById('chatHistory');
  const historyPlaceholder = document.getElementById('historyPlaceholder');
  const currentChatTitle = document.getElementById('currentChatTitle');

  let conversationId = generateId();
  const history = new Map(); // id -> { title, messages }
  let activeHistoryId = null;

  function generateId() {
    return 'conv_' + Date.now() + '_' + Math.random().toString(36).slice(2, 9);
  }

  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  function formatMessage(text) {
    const escaped = escapeHtml(text);
    return escaped
      .replace(/```(\w*)\n([\s\S]*?)```/g, (_, _lang, code) => '<pre><code>' + code + '</code></pre>')
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/\n/g, '<br>');
  }

  function addMessage(role, content, options = {}) {
    const id = options.id || generateId();
    const div = document.createElement('div');
    div.className = 'message ' + role + (options.loading ? ' loading' : '');
    div.dataset.messageId = id;

    let bodyHtml;
    if (role === 'assistant' && !options.loading) {
      const text = typeof content === 'string' ? content : (content.response || '');
      const items = options.items || (typeof content === 'object' && content.items) || [];
      bodyHtml = formatMessage(text) + renderItems(items);
    } else {
      bodyHtml = escapeHtml(typeof content === 'string' ? content : (content.response || content));
    }

    const headerLabel = role === 'assistant' ? 'Assistant' : '';
    div.innerHTML =
      (headerLabel ? '<div class="message-header">' + headerLabel + '</div>' : '') +
      '<div class="message-body">' + bodyHtml + '</div>';

    if (role === 'user' && !options.loading) {
      div.setAttribute('data-user-message', '');
      div.title = 'Click to use this message in input';
      div.addEventListener('click', () => {
        queryInput.value = content;
        resizeTextarea();
        queryInput.focus();
      });
    }

    welcomeScreen.style.display = 'none';
    messagesEl.appendChild(div);
    return div;
  }

  function setLoading(show) {
    btnSend.disabled = show;
  }

  const API_BASE = 'http://localhost:9080';

  async function fetchResponse(query) {
    const url = API_BASE + '/api/v1/jarvis/query?q=' + encodeURIComponent(query);
    const res = await fetch(url, { method: 'GET' });

    const contentType = res.headers.get('content-type') || '';
    const body = contentType.includes('application/json') ? await res.json() : null;

    if (!res.ok) {
      const detail = body?.detail || body?.title || ('HTTP ' + res.status);
      throw new Error(detail);
    }

    const text = body?.response;
    if (text == null) throw new Error('No response in server reply');
    return { response: text, items: body?.items || [] };
  }

  function getProviderIcon(provider) {
    const p = (provider || '').toUpperCase();
    if (p === 'GITHUB') {
      return '<svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/></svg>';
    }
    if (p === 'GOOGLE_BOOKS') {
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/><line x1="8" y1="7" x2="16" y2="7"/><line x1="8" y1="11" x2="16" y2="11"/><line x1="8" y1="15" x2="12" y2="15"/></svg>';
    }
    if (p === 'BOOKSTORE') {
      return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>';
    }
    return '<span class="provider-text">' + escapeHtml(provider || '') + '</span>';
  }

  function formatShortDescription(text) {
    if (!text) return '';
    const trimmed = text.replace(/^[\s\n]+|[\s\n]+$/g, '');
    const looksLikeCode = text.includes('\n') || /^[\s`{}\[\]<>]|^[a-zA-Z_]+\(|^import |^function |^class |^def |^public |^private /.test(text.trim());
    if (looksLikeCode) {
      return '<pre><code>' + trimmed + '</code></pre>';
    }
    return trimmed.replace(/\n/g, '<br>');
  }

  function renderItems(items) {
    if (!items || items.length === 0) return '';
    let html = '<div class="response-items">';
    for (const item of items) {
      const header = item.header || {};
      const title = header.title || 'Link';
      const href = header.href || '#';
      const desc = item.shortDescription || '';
      const provider = item.searchProvider || '';
      html += '<div class="response-item">' +
        '<div class="response-item-header">' +
        '<span class="provider-icon provider-' + (provider || 'unknown').toLowerCase().replace(/_/g, '-') + '" title="' + escapeHtml(provider) + '">' + getProviderIcon(provider) + '</span>' +
        '<a class="response-item-link" href="' + escapeHtml(href) + '" target="_blank" rel="noopener noreferrer">' + escapeHtml(title) + '</a>' +
        '</div>' +
        '<div class="response-item-desc">' + formatShortDescription(desc) + '</div>' +
        '</div>';
    }
    html += '</div>';
    return html;
  }

  function getTitleFromQuery(query) {
    return query.slice(0, 40) + (query.length > 40 ? '…' : '');
  }

  function scrollQueryIntoViewSmooth(messageEl) {
    if (!messageEl) return;
    const start = messagesEl.scrollTop;
    const targetTop = messageEl.offsetTop - 40;
    const target = targetTop > 0 ? targetTop : 0;
    const distance = target - start;
    if (Math.abs(distance) < 4) return;

    const duration = 350;
    let startTime = null;

    function step(timestamp) {
      if (!startTime) startTime = timestamp;
      const elapsed = timestamp - startTime;
      const t = elapsed / duration;
      const eased = t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t; // easeInOut
      messagesEl.scrollTop = start + distance * Math.min(Math.max(eased, 0), 1);
      if (elapsed < duration) {
        requestAnimationFrame(step);
      }
    }

    requestAnimationFrame(step);
  }

  async function sendQuery(query) {
    query = query.trim();
    if (!query) return;

    const userMessageEl = addMessage('user', query);
    queryInput.value = '';
    resizeTextarea();

    const loadingEl = addMessage('assistant', 'Thinking…', { loading: true });
    setLoading(true);

    try {
      const data = await fetchResponse(query);
      loadingEl.remove();
      addMessage('assistant', data.response, { items: data.items });
      scrollQueryIntoViewSmooth(userMessageEl);

      if (activeHistoryId) {
        const h = history.get(activeHistoryId);
        if (h) {
          h.messages.push({ role: 'user', content: query });
          h.messages.push({ role: 'assistant', content: data.response, items: data.items });
        }
      } else {
        const title = getTitleFromQuery(query);
        const conv = {
          title,
          messages: [
            { role: 'user', content: query },
            { role: 'assistant', content: data.response, items: data.items }
          ]
        };
        history.set(conversationId, conv);
        activeHistoryId = conversationId;
        currentChatTitle.textContent = title;
        renderHistory();
      }
    } catch (err) {
      loadingEl.remove();
      addMessage('assistant', 'Error: ' + (err.message || 'Request failed.'));
    } finally {
      setLoading(false);
    }
  }

  function renderHistory() {
    historyPlaceholder.style.display = history.size ? 'none' : 'block';
    const existing = chatHistory.querySelectorAll('.history-item');
    existing.forEach((el) => el.remove());

    for (const [id, conv] of history) {
      const btn = document.createElement('button');
      btn.className = 'history-item' + (id === activeHistoryId ? ' active' : '');
      btn.textContent = conv.title;
      btn.type = 'button';
      btn.addEventListener('click', () => loadConversation(id));
      chatHistory.appendChild(btn);
    }
  }

  function loadConversation(id) {
    const conv = history.get(id);
    if (!conv) return;

    activeHistoryId = id;
    conversationId = id;
    currentChatTitle.textContent = conv.title;

    welcomeScreen.style.display = 'none';
    const userMsgs = messagesEl.querySelectorAll('.message');
    userMsgs.forEach((el) => el.remove());

    conv.messages.forEach((m) => addMessage(m.role, m.content, { items: m.items }));
    renderHistory();
  }

  function startNewChat() {
    conversationId = generateId();
    activeHistoryId = null;
    currentChatTitle.textContent = 'New conversation';
    welcomeScreen.style.display = 'flex';

    const userMsgs = messagesEl.querySelectorAll('.message');
    userMsgs.forEach((el) => el.remove());
    renderHistory();
    queryInput.focus();
  }

  function resizeTextarea() {
    queryInput.style.height = 'auto';
    queryInput.style.height = Math.min(queryInput.scrollHeight, 200) + 'px';
  }

  inputForm.addEventListener('submit', (e) => {
    e.preventDefault();
    sendQuery(queryInput.value);
  });

  queryInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendQuery(queryInput.value);
    }
  });

  queryInput.addEventListener('input', resizeTextarea);

  btnNewChat.addEventListener('click', startNewChat);

  document.querySelectorAll('.quick-prompt').forEach((btn) => {
    btn.addEventListener('click', () => sendQuery(btn.dataset.query));
  });

  queryInput.focus();
})();

