DirectOutputProvider = function (data, downloadFileName) {
    let loadCallback = () => {
    };
    let hasSentData = false;

    return {
        loadData: loadData,
        registerLoadCallback: registerLoadCallback,
        getData: () => data,
        download: download
    };

    async function loadData() {
        if (hasSentData) return;
        hasSentData = true;
        loadCallback(data);
    }

    function download(fileName) {
        let name = "download.txt";
        if (fileName) {
            name = fileName
        } else if (downloadFileName) {
            name = downloadFileName
        }


        // from: https://stackoverflow.com/questions/3665115/create-a-file-in-memory-for-user-to-download-not-through-server
        const blob = new Blob([data], {type: 'application/octet-stream'});
        if (window.navigator.msSaveOrOpenBlob) {
            window.navigator.msSaveBlob(blob, name);
        }
        else {
            const elem = window.document.createElement('a');
            elem.href = window.URL.createObjectURL(blob);
            elem.download = name;
            document.body.appendChild(elem);
            elem.click();
            document.body.removeChild(elem);
        }

    }

    function registerLoadCallback(callback) {
        loadCallback = callback;
    }


};