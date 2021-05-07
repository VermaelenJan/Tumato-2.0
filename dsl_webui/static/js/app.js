init_app = function (xtextUrl, xtextPrefix, specificationName, specificationID) {
    let changedSinceSave = false;

    let baseUrl = window.location.pathname;
    let generatingPolicy = false;

    const policyViewer = OutputViewBuilder("policy-view", "text");
    let policyProvider = undefined;
    const policyDownloadFileName = specificationName + ".json";

    const jsonSpecViewer = OutputViewBuilder("json-spec-view", "javascript");
    let jsonSpecProvider = undefined;
    const jsonSpecDownloadFileName = specificationName + ".json";

    const protoSpecViewer = OutputViewBuilder("proto-spec-view", "text");
    let protoSpecProvider = undefined;
    const protoSpecDownloadFileName = specificationName + ".buf";
    const ErrorViewer = ErrorViewBuilder();

    baseUrl = xtextUrl + "/";
    require.config({
        baseUrl: baseUrl,
        paths: {
            "text": "webjars/requirejs-text/2.0.15/text",
            "jquery": "/static/js/jquery-3.3.1.min",
            "xtext/xtext-orion": "xtext/2.12.0/xtext-orion"
        }
    });
    let editor = new Promise((resolve, reject) => {
        require([xtextUrl + "/orion/code_edit/built-codeEdit-amd.js"], function () {
            require(["xtext/xtext-orion"], function (xtext) {
                xtext.createEditor({
                    baseUrl: baseUrl,
                    serviceUrl: baseUrl + xtextPrefix,
                    enableCors: false,
                    syntaxDefinition: xtextUrl + "/xtext-resources/generated/tmt-syntax.js"
                }).then(resolve);
            });
        });
    });
    editor.then(editor => postEditorInit(editor));

    const activateOutputTab = TabView("output-tab-wrapper");
    activateOutputTab("json");

    return {
        generatePolicy: generatePolicy,
        generateSpec: generateSpec,
        activateOutputTab: activateOutputTab,
        saveText: saveText,
        loadText: loadText,
        downloadPolicy: downloadPolicy,
        editor: editor,
    };

    async function generatePolicy(force_fresh) {
        if (generatingPolicy) {
            return;
        }
        const localEditor = await editor;
        const state = localEditor.xtextServices.editorContext.getServerState().stateId;
        if (state === undefined || !specificationID) {
            return;
        }
        await saveText();
        generatingPolicy = true;
        const policySpinner = document.getElementById("policy-spinner");
        const policyButton = document.getElementById("policy-button");
        const resourceId = localEditor.xtextServices.options.resourceId;
        const evtSrc = new EventSource("/policy/compile?resource=" + resourceId + "&requiredStateId=" + state + "&specId=" + specificationID + "&force=" + force_fresh);
        ErrorViewer.deInit();
        evtSrc.addEventListener("policy-update", async function (e) {
            const resp = JSON.parse(e.data);
            console.log(resp.message);

            if (resp.tag === "QUEUED") {
                policySpinner.className = "queued";
                policyButton.classList.add("activated");
            } else if (resp.tag === "STARTED") {
                policySpinner.className = "started";
                policyButton.classList.add("activated");
            } else {
                policySpinner.className = "";
                policyButton.classList.remove("activated");
            }

            if (resp.tag === "SUCCESS" || resp.tag === "FAILURE") {
                evtSrc.close();
                generatingPolicy = false;
                activateOutputTab("policy");
            }

            if (resp.tag === "SUCCESS") {
                ErrorViewer.deInit();
                policyProvider = RemoteOutputProvider("policy/" + resp.data, true, policyDownloadFileName);
                await policyViewer.init(policyProvider);
            }
            if (resp.tag === "FAILURE") {
                policyViewer.deInit();
                if (resp.data.infeasible_model_failure) {
                    ErrorViewer.initInfeasibleModel(resp.data.model);
                } else {
                    ErrorViewer.initErrorMessage(resp.message);
                }
            }

        });

        evtSrc.onerror = function (e) {
            evtSrc.close();
            generatingPolicy = false;
            policyViewer.deInit();
            ErrorViewer.initConnectionLost();
        }
    }

    function reportError(message) {
        alert(message);
    }


    async function saveText() {
        const text = await getEditorText();
        let savePromise = new Promise((resolve, reject) =>
            jQuery.post(
                "spec/" + specificationID,
                {text: text}
            ).done(resolve).fail(reject)
        );
        await savePromise;
        saveSpec(specificationName, specificationID);
        // localStorage.setItem('save', text);
        changedSinceSave = false;
    }

    async function loadText() {
        const localEditor = await editor;
        let loadPromise = new Promise((resolve, reject) =>
            jQuery.get(
                "spec/" + specificationID,
            ).done((data, status) => resolve(data)).fail(reject)
        );
        try {
            const text = await loadPromise;
            localEditor.editor.setText(text);
        } catch (error) {
            if (error.status === 404) {
                alert("Could not load specification! The URL of this page may be malformed.");
            } else {
                alert("An unknown error occurred while loading the specification.");
            }
        }
        await updateXtextSpec();
        changedSinceSave = false;


    }

    async function postEditorInit(editor) {
        await loadText();
        registerChangeListener(editor);
        registerUnloadWarning();
        return updateSpecOutputs();

    }

    async function getEditorText() {
        const localEditor = await editor;
        return localEditor.editor.getText();
    }

    function registerChangeListener(editor) {
        editor.editor.getModel().addEventListener("Changed", e => changedSinceSave = true);
        editor.editor.getModel().addEventListener("Changed", updateSpecOutputs);

    }

    function registerUnloadWarning() {
        window.onbeforeunload = e => {
            if (changedSinceSave)
                return 'There are unsaved changes in the specification. ' +
                    'These will be lost if you navigate away from this page.';
        }
    }

    async function generateSpec(artifact_name) {
        if (artifact_name === undefined) {
            artifact_name = "spec.json"
        }
        const localEditor = await editor;
        return new Promise((resolve, reject) => {
            localEditor.xtextServices.generate({"artifactId": artifact_name})
                .then(resolve, reject);
        });
    }

    async function updateXtextSpec() {
        const localEditor = await editor;
        return new Promise((resolve, reject) =>
            localEditor.xtextServices.update().then(resolve).catch(reject)
        );
    }

    async function updateSpecOutputs() {
        await updateXtextSpec();
        const jsonPromise = generateSpec("spec.json");
        const protoPromise = generateSpec("spec.buf");
        const data = await Promise.all([jsonPromise, protoPromise]);
        jsonSpecProvider = DirectOutputProvider(data[0], jsonSpecDownloadFileName);
        const jsonSpecViewPromise = jsonSpecViewer.init(jsonSpecProvider);

        protoSpecProvider = DirectOutputProvider(data[1], protoSpecDownloadFileName);
        const protoSpecViewPromise = protoSpecViewer.init(protoSpecProvider);

        return Promise.all([jsonSpecViewPromise, protoSpecViewPromise])
    }


    function downloadPolicy() {
        if (policyProvider !== undefined) {
            policyProvider.download();
        }
    }

    function downloadJsonSpec() {
        if (jsonSpecProvider !== undefined) {
            jsonSpecProvider.download();
        }
    }

    function downloadProtoSpec() {
        if (protoSpecProvider !== undefined) {
            protoSpecProvider.download();
        }
    }

};
