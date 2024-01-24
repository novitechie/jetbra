// ==UserScript==
// @name         JetBra
// @namespace    https://github.com/novice88/jetbra
// @version      3.0
// @license MIT
// @description  Add a button on the plugin homepage and click to get the plugin activation code
// @author       novice.li
// @match        https://plugins.jetbrains.com/plugin/*
// @grant        GM_setClipboard
// @grant        GM_addStyle
// @grant        GM_xmlhttpRequest
// @grant        window.onurlchange
// @connect noviceli.win
// @connect self
// @connect localhost
// ==/UserScript==


async function findElementWithRetry(cssSelector) {
    const maxAttempts = 50;
    for (let attempts = 0; attempts < maxAttempts; attempts++) {
        const element = document.querySelector(cssSelector);
        if (element) {
            return element;
        }
        await new Promise(resolve => setTimeout(resolve, 100));
    }
    throw new Error(`Element with selector '${cssSelector}' not found after ${maxAttempts} attempts.`);
}

let addButton = async function () {
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

    const parentElement = await findElementWithRetry('.plugin-header__controls-panel > div:first-child');

    // 如果 parentElement 的孩子中已经有了按钮，就不再添加
    if (parentElement.querySelector('.jetbra-button')) {
        return;
    }
    let newElement = document.createElement('div');
    newElement.classList.toggle('wt-col-inline');
    newElement.innerHTML = `<button class="jetbra-button" type="button">CLICK TO GENERATE ACTIVATION CODE</button>`;
    parentElement.appendChild(newElement)

    newElement.addEventListener('click', async () => {
        if (pluginDetail.purchaseInfo === undefined) {
            window.alert('This plugin is not a paid plugin in the market');
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
                window.alert('The activation code has been copied to your clipboard');
            }
        });
    })
};
window.onload = function () {
    addButton();
}
if (window.onurlchange === null) {
    window.addEventListener('urlchange', (info) => {
        addButton();
    });
}