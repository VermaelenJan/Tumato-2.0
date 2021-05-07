RemoteOutputProvider = function (dataLocation, partial=false, downloadFileName) {
    const NUM_LOAD_LINES = 1000;
    let loadCallback = () => {};
    let isLoading = false;
    let currentLine = 0;
    let hasMore = true;
    let dataCache = "";
    let loadedAtLeastOnce = false;

    return {
        loadData: loadData,
        registerLoadCallback: registerLoadCallback,
        getData: () => dataCache,
        download: download
    };

    async function loadData() {
        if(partial){
            await loadNextPart();
        }else{
            await loadAllData();
        }
    }

    async function loadAllData() {
        if(isLoading) return;
        if(loadedAtLeastOnce) return;
        let loadPromise = new Promise((resolve, reject) =>
            jQuery.get(
                dataLocation,
            ).done((data, status) => resolve(data)).fail(reject)
        );
        const result = await loadPromise;
        dataCache += result.text;

        loadCallback(dataCache);
        loadedAtLeastOnce = true;
    }

    async function loadNextPart() {
        if (isLoading) return;
        if(!hasMore) return;
        isLoading = true;

        let loadPromise = new Promise((resolve, reject) =>
            jQuery.get(
                dataLocation + "/part/" + currentLine + "/" + NUM_LOAD_LINES,
            ).done((data, status) => resolve(data)).fail(reject)
        );

        const policyResult = await loadPromise;

        dataCache += policyResult.text;
        currentLine = policyResult.next_line;
        hasMore = policyResult.has_more;

        loadCallback(dataCache);

        isLoading = false;
        loadedAtLeastOnce = true;
    }

    function download(fileName) {
        let name = "download.txt";
        if(fileName){
            name = fileName
        }else if(downloadFileName){
            name = downloadFileName
        }
        downloadUrl(dataLocation + "/" + downloadFileName + "/download", name);
    }

    function registerLoadCallback(callback) {
        loadCallback = callback;
    }

};