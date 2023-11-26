// ==UserScript==
// @name         JetBra
// @namespace    https://github.com/novice88/jetbra
// @version      1.1
// @license MIT
// @description  添加一个按钮,点击获取插件的激活码
// @author       novice.li
// @match        https://plugins.jetbrains.com/plugin/*
// @grant        GM_setClipboard
// @grant        GM_addStyle
// @grant        GM_xmlhttpRequest
// @connect noviceli.win
// @connect self
// @connect localhost
// ==/UserScript==

var elmGetter = function() {
    const win = window.unsafeWindow || document.defaultView || window;
    const doc = win.document;
    const listeners = new WeakMap();
    let mode = 'css';
    let $;
    const elProto = win.Element.prototype;
    const matches = elProto.matches ||
        elProto.matchesSelector ||
        elProto.webkitMatchesSelector ||
        elProto.mozMatchesSelector ||
        elProto.oMatchesSelector;
    const MutationObs = win.MutationObserver ||
        win.WebkitMutationObserver ||
        win.MozMutationObserver;
    function addObserver(target, callback) {
        const observer = new MutationObs(mutations => {
            for (const mutation of mutations) {
                if (mutation.type === 'attributes') {
                    callback(mutation.target);
                    if (observer.canceled) return;
                }
                for (const node of mutation.addedNodes) {
                    if (node instanceof Element) callback(node);
                    if (observer.canceled) return;
                }
            }
        });
        observer.canceled = false;
        observer.observe(target, {childList: true, subtree: true, attributes: true});
        return () => {
            observer.canceled = true;
            observer.disconnect();
        };
    }
    function addFilter(target, filter) {
        let listener = listeners.get(target);
        if (!listener) {
            listener = {
                filters: new Set(),
                remove: addObserver(target, el => listener.filters.forEach(f => f(el)))
            };
            listeners.set(target, listener);
        }
        listener.filters.add(filter);
    }
    function removeFilter(target, filter) {
        const listener = listeners.get(target);
        if (!listener) return;
        listener.filters.delete(filter);
        if (!listener.filters.size) {
            listener.remove();
            listeners.delete(target);
        }
    }
    function query(all, selector, parent, includeParent, curMode) {
        switch (curMode) {
            case 'css':
                const checkParent = includeParent && matches.call(parent, selector);
                if (all) {
                    const queryAll = parent.querySelectorAll(selector);
                    return checkParent ? [parent, ...queryAll] : [...queryAll];
                }
                return checkParent ? parent : parent.querySelector(selector);
            case 'jquery':
                let jNodes = $(includeParent ? parent : []);
                jNodes = jNodes.add([...parent.querySelectorAll('*')]).filter(selector);
                if (all) return $.map(jNodes, el => $(el));
                return jNodes.length ? $(jNodes.get(0)) : null;
            case 'xpath':
                const ownerDoc = parent.ownerDocument || parent;
                selector += '/self::*';
                if (all) {
                    const xPathResult = ownerDoc.evaluate(selector, parent, null, 7, null);
                    const result = [];
                    for (let i = 0; i < xPathResult.snapshotLength; i++) {
                        result.push(xPathResult.snapshotItem(i));
                    }
                    return result;
                }
                return ownerDoc.evaluate(selector, parent, null, 9, null).singleNodeValue;
        }
    }
    function isJquery(jq) {
        return jq && jq.fn && typeof jq.fn.jquery === 'string';
    }
    function getOne(selector, parent, timeout) {
        const curMode = mode;
        return new Promise(resolve => {
            const node = query(false, selector, parent, false, curMode);
            if (node) return resolve(node);
            let timer;
            const filter = el => {
                const node = query(false, selector, el, true, curMode);
                if (node) {
                    removeFilter(parent, filter);
                    timer && clearTimeout(timer);
                    resolve(node);
                }
            };
            addFilter(parent, filter);
            if (timeout > 0) {
                timer = setTimeout(() => {
                    removeFilter(parent, filter);
                    resolve(null);
                }, timeout);
            }
        });
    }
    return {
        get currentSelector() {
            return mode;
        },
        get(selector, ...args) {
            let parent = typeof args[0] !== 'number' && args.shift() || doc;
            if (mode === 'jquery' && parent instanceof $) parent = parent.get(0);
            const timeout = args[0] || 0;
            if (Array.isArray(selector)) {
                return Promise.all(selector.map(s => getOne(s, parent, timeout)));
            }
            return getOne(selector, parent, timeout);
        },
        each(selector, ...args) {
            let parent = typeof args[0] !== 'function' && args.shift() || doc;
            if (mode === 'jquery' && parent instanceof $) parent = parent.get(0);
            const callback = args[0];
            const curMode = mode;
            const refs = new WeakSet();
            for (const node of query(true, selector, parent, false, curMode)) {
                refs.add(curMode === 'jquery' ? node.get(0) : node);
                if (callback(node, false) === false) return;
            }
            const filter = el => {
                for (const node of query(true, selector, el, true, curMode)) {
                    const _el = curMode === 'jquery' ? node.get(0) : node;
                    if (refs.has(_el)) break;
                    refs.add(_el);
                    if (callback(node, true) === false) {
                        return removeFilter(parent, filter);
                    }
                }
            };
            addFilter(parent, filter);
        },
        create(domString, ...args) {
            const returnList = typeof args[0] === 'boolean' && args.shift();
            const parent = args[0];
            const template = doc.createElement('template');
            template.innerHTML = domString;
            const node = template.content.firstElementChild;
            if (!node) return null;
            parent ? parent.appendChild(node) : node.remove();
            if (returnList) {
                const list = {};
                node.querySelectorAll('[id]').forEach(el => list[el.id] = el);
                list[0] = node;
                return list;
            }
            return node;
        },
        selector(desc) {
            switch (true) {
                case isJquery(desc):
                    $ = desc;
                    return mode = 'jquery';
                case !desc || typeof desc.toLowerCase !== 'function':
                    return mode = 'css';
                case desc.toLowerCase() === 'jquery':
                    for (const jq of [window.jQuery, window.$, win.jQuery, win.$]) {
                        if (isJquery(jq)) {
                            $ = jq;
                            break;
                        };
                    }
                    return mode = $ ? 'jquery' : 'css';
                case desc.toLowerCase() === 'xpath':
                    return mode = 'xpath';
                default:
                    return mode = 'css';
            }
        }
    };
}();
(async function () {
    'use strict';
    GM_addStyle(`
    .jetbra-button {
        background-color: #04AA6D;
        border: none;
        color: white;
        padding: 8px 24px;
        text-align: center;
        text-decoration: none;
        display: inline-block;
        border-radius: 16px;
        box-shadow: 0 8px 16px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
        transition-duration: 0.4s;
    }

    .jetbra-button:hover {
        background-color: #057e47;
        color: white;
    }
`);
    const backendBaseUrl = 'https://jetbra.noviceli.win'
    const metaTag = document.querySelector('meta[name="pluginId"]')
    if (!metaTag) {
        return
    }
    const pluginId = metaTag.getAttribute('content')

    let pluginDetail = await fetch('https://plugins.jetbrains.com/api/plugins/' + pluginId).then(r => r.json());

    const parentElement = await elmGetter.get('.plugin-header__controls-panel > div:first-child');

    let newElement = document.createElement('div');
    newElement.classList.toggle('wt-col-inline');
    newElement.innerHTML = `<button class="jetbra-button" type="button">点击生成激活码</button>`;
    parentElement.appendChild(newElement)


    newElement.addEventListener('click', async () => {
        if (pluginDetail.purchaseInfo === undefined) {
            window.alert('此插件不是付费插件');
            return;
        }
        let data = {
            "licenseeName": "Test",
            "assigneeName": "novice.li",
            "assigneeEmail": "",
            "licenseRestriction": "",
            "checkConcurrentUse": false,
            "products": [{
                "code": pluginDetail.purchaseInfo.productCode,
                "fallbackDate": "2099-12-30",
                "paidUpTo": "2099-12-30",
                "extended": false
            }],
            "metadata": "0120230102PPAA013009",
            "hash": "41472961/0:1563609451",
            "gracePeriodDays": 7,
            "autoProlongated": true,
            "isAutoProlongated": true
        }
        GM_xmlhttpRequest({
            method: 'POST',
            url: backendBaseUrl + '/generateLicense',
            headers: {
                'Content-Type': 'application/json'
            },
            data: JSON.stringify(data),
            onload: function (response) {
                let license = JSON.parse(response.responseText).license
                GM_setClipboard(license, 'text');
                window.alert('激活码已复制到剪切版');
            }
        });
    })
})();
