PolicyViewBuilder = function () {
    const NUM_LOAD_LINES = 1000;
    const AUTO_LOAD_SCREEN_OFFSET = 3;
    const outputView = document.getElementById("policy-view");

    let currentPolicyHash = undefined;
    let isLoading = false;
    let currentLine = 0;
    let policyCache = "";
    let hasMore = true;

    return {
        initPolicy: initPolicy,
        deInit: deInit,
        downloadPolicy: downloadPolicy
    };

    async function initPolicy(policy_hash) {
        currentPolicyHash = policy_hash;
        removeScrollViewEventListener();
        isLoading = false;
        currentLine = 0;
        policyCache = "";
        hasMore = true;

        await loadNextPolicyPart();
    }

    function deInit() {
        removeScrollViewEventListener();
        currentPolicyHash = undefined;
        outputView.innerHTML = "";
    }

    function downloadPolicy() {
        if (!isInitialized()) alert("You need to generate the policy first!");
        downloadUrl('policy/' + currentPolicyHash + "/download", currentPolicyHash);
    }

    async function loadNextPolicyPart() {
        if (!isInitialized()) alert("You need to generate the policy first!");
        if (isPolicyLoading()) return;
        isLoading = true;

        let loadPromise = new Promise((resolve, reject) =>
            jQuery.get(
                "policy/" + currentPolicyHash + "/part/" + currentLine + "/" + NUM_LOAD_LINES,
            ).done((data, status) => resolve(data)).fail(reject)
        );

        const policyResult = await loadPromise;

        policyCache += policyResult.text;
        currentLine = policyResult.next_line;
        hasMore = policyResult.has_more;

        removeScrollViewEventListener();
        const scrollPosition = getScrollViewScrollTop();

        renderScrollView();

        addScrollViewEventListener();
        setScrollViewScrollTop(scrollPosition);

        isLoading = false;

    }

    function isInitialized() {
        return currentPolicyHash !== undefined;
    }

    function isPolicyLoading() {
        return isLoading;
    }

    async function policyViewEventListener(event) {
        if (!hasMore) return;
        const elem = event.target;
        const scroll_position = elem['scrollTop'];
        const scroll_height = elem['scrollHeight'];
        const elem_height = elem.clientHeight;
        const scroll_bottom = scroll_height - elem_height - scroll_position;
        if (scroll_bottom < AUTO_LOAD_SCREEN_OFFSET * elem_height) {
            await loadNextPolicyPart();
        }
    }

    function getScrollView() {
        return outputView.querySelector(".syntaxhighlighter");
    }

    function removeScrollViewEventListener() {
        const scrollView = getScrollView();
        if (scrollView) {
            scrollView.removeEventListener('scroll', policyViewEventListener);
        }
    }

    function addScrollViewEventListener() {
        const scrollView = getScrollView();
        if (scrollView) {
            scrollView.addEventListener('scroll', policyViewEventListener);
        }
    }

    function getScrollViewScrollTop() {
        const scrollView = getScrollView();
        if (scrollView) {
            return scrollView.scrollTop;
        }
        return 0;
    }

    function setScrollViewScrollTop(scrollPosition) {
        const scrollView = getScrollView();
        if (scrollView) {
            return scrollView.scrollTop = scrollPosition;
        }
    }

    function renderScrollView() {
        const el = document.createElement("pre");
        el.classList += "brush:javascript";
        el.innerHTML = policyCache;

        outputView.innerHTML = '';
        outputView.appendChild(el);
        SyntaxHighlighter.highlight();
    }


};