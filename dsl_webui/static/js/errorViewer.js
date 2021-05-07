ErrorViewBuilder = function () {
    const outputView = document.getElementById("error-view");

    return {
        initConnectionLost: initConnectionLost,
        initInfeasibleModel: initInfeasibleModel,
        initUnknownError: initUnknownError,
        initErrorMessage: initErrorMessage,
        deInit: deInit,
    };

    function initConnectionLost() {
        outputView.innerHTML = "<h1>The connection to the server has been lost.</h1>"
    }

    function initInfeasibleModel(model) {
        outputView.innerHTML = "<h1>The specification is infeasible.</h1>" +
            "<p>There is no feasible behavior for the following state:<p>";
        renderModel(model, outputView);
    }

    function initUnknownError() {
        outputView.innerHTML = "<h1>An unknown error occurred. Please try again.</h1>"
    }
    function initErrorMessage(message) {
        outputView.innerHTML = "<h1>Error: " + message + "</h1>"
    }

    function deInit() {
        outputView.innerHTML = "";
    }

    function renderModel(model, parentNode) {
        const wrapper = document.createElement("div");
        wrapper.classList += "model-wrapper";
        for (const key of Object.keys(model)) {
            const stateVarWrapper = document.createElement("div");
            stateVarWrapper.classList += "state-var-wrapper";

            const stateVarName = document.createElement("span");
            stateVarName.classList += "state-var-name";
            stateVarName.innerHTML = key;

            const stateVarValue = document.createElement("span");
            stateVarValue.classList += "state-var-value";
            stateVarValue.innerHTML = model[key];

            stateVarWrapper.appendChild(stateVarName);
            stateVarWrapper.appendChild(stateVarValue);
            wrapper.appendChild(stateVarWrapper);
        }
        parentNode.appendChild(wrapper);
    }
};