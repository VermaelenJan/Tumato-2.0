OutputViewBuilder = function (view_id, type) {
    const AUTO_LOAD_SCREEN_OFFSET = 3;
    const outputView = document.getElementById(view_id);

    let dataProvider = undefined;

    return {
        init: init,
        deInit: deInit,
    };

    async function init(outputProvider) {
        removeScrollViewEventListener();
        dataProvider = outputProvider;
        dataProvider.registerLoadCallback(loadDataCallback);

        await dataProvider.loadData();
    }

    function deInit() {
        removeScrollViewEventListener();
        dataProvider = undefined;
        outputView.innerHTML = "";
    }

    function loadDataCallback(data){
        removeScrollViewEventListener();
        const scrollPosition = getScrollViewScrollTop();

        renderScrollView(data);

        addScrollViewEventListener();
        setScrollViewScrollTop(scrollPosition);

        displayDownloadButton();
    }

    async function outputViewEventListener(event) {
        const elem = event.target;
        const scroll_position = elem['scrollTop'];
        const scroll_height = elem['scrollHeight'];
        const elem_height = elem.clientHeight;
        const scroll_bottom = scroll_height - elem_height - scroll_position;
        if (scroll_bottom < AUTO_LOAD_SCREEN_OFFSET * elem_height) {
            dataProvider.loadData();
        }
    }

    function renderScrollView(data) {
        const el = document.createElement("pre");
        el.classList += "brush:" + type;
        el.innerHTML = preProcessData(data);

        outputView.innerHTML = '';
        outputView.appendChild(el);
        SyntaxHighlighter.highlight();
    }

    function getScrollView() {
        return outputView.querySelector(".syntaxhighlighter");
    }

    function removeScrollViewEventListener() {
        const scrollView = getScrollView();
        if (scrollView) {
            scrollView.removeEventListener('scroll', outputViewEventListener);
        }
    }

    function addScrollViewEventListener() {
        const scrollView = getScrollView();
        if (scrollView) {
            scrollView.addEventListener('scroll', outputViewEventListener);
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

    function preProcessData(data){
        if(typeof type !== "string"){
            return data;
        }
        let local_type = type.toLowerCase();

        if(local_type === "javascript" || local_type === "js"){
            return JSON.stringify(JSON.parse(data), undefined, 2);
        }

        return data;
    }

    function displayDownloadButton(){
        if(!outputView.querySelector(".download-button")){
            const downloadButton = document.createElement("div");
            downloadButton.classList.add("download-button");
            downloadButton.innerHTML = "Download";
            outputView.appendChild(downloadButton);
            downloadButton.addEventListener("click", function () {
                if(dataProvider){
                    dataProvider.download();
                }
            })
        }
    }

};