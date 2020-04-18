//														***** global objects *****
// -> title
var titleModal = {};
titleModal.idTitle = null;
titleModal.name = null;
// -> category
var categoryModal = {};
categoryModal.idCategory = null;
categoryModal.name = null;
categoryModal.title = {};
// -> absence
var absenceModal = {};
absenceModal.idAbsence = null;
absenceModal.name = null;
absenceModal.listCategoryAbsence = null;
// -> subdivision
var subdivisionModal = {};
subdivisionModal.idSubdivision = null;
subdivisionModal.name = null;
subdivisionModal.parent = null;
// -> user
var userModal = {};
userModal.idUser = null;
userModal.name = null;
userModal.roleSystem = null;
userModal.password = null;
userModal.listUserSubdivision = null;
// -> base expenditure
var idExpenditureBase = null;
var subdivisionNameBase = null;
//														***** programm's function *****
// -> ОТОБРАЖЕНИЕ УВЕДОМЛЕНИЙ И ОШИБОК
function showNotice(theme, definition) {
    $("#noticeWindow").find("#theme").text(theme);
    $("#noticeWindow").find("#definition").text(definition);
    $("#noticeWindow").modal({
       show : true,
       backdrop: 'static', 
       keyboard: false
    });
};

// -> БЛОКИРОВКА ВВОДА НЕКОРРЕКТНЫХ ДАННЫХ РАСХОДОВ
//-------------- START ---------------
function checkTables() {
    let rows = document.querySelectorAll('div#content > table > tbody > tr');
    let button = searchButtons();
    if (rows.length) {
        for (let i = 0; i < rows.length; i++) {
            let cellState = rows[i].cells[1],
                cellList = rows[i].cells[2] || null,
                cellFace = rows[i].cells[3] || null,
                cellDont = rows[i].cells[4] || null;
            if (cellList) {
                isNaN(+cellList.innerHTML) ? cellList = cellList.querySelector('input').value : cellList = cellList.innerHTML;
                if ((+cellState.innerHTML) >= +cellList) {
                    rows[i].style.backgroundColor = 'white';
                    if (cellFace && cellDont) {
                        cellFace = cellFace.querySelector('input').value;
                        cellDont = cellDont.querySelectorAll('table input');
                        let sumLastColumn = Array.from(cellDont).reduce(function (sum, current) {
                            return sum + (+current.value);
                        }, 0);
                        if (cellList != sumLastColumn + (+cellFace)) {
                            rows[i].style.backgroundColor = 'red';
                            button.disabled = searchRed();
                        } else {
                            rows[i].style.backgroundColor = 'white';
                            button.disabled = searchRed();
                        }
                    } else {
                        button.disabled = searchRed();
                    }
                } else {
                    rows[i].style.backgroundColor = 'red';
                    button.disabled = searchRed();
                }
            }            
            listenEvent(rows[i], checkTables);
        }
    }
}

function searchWord(e) {   
    let reg = new RegExp('\D', 'g');
    let elem = e.target;
    return elem.value = elem.value.replace(reg, '');
}

function searchRed() {
    let tables = document.querySelectorAll('div#content > table');
    let flag = false;
    for (let i = 0; i < tables.length; i++) {
        flag = tables[i].innerHTML.includes('background-color: red');
        if (flag) return flag;
    }
    return flag;
}

function searchButtons() {
    return document.querySelector('input[type="button"][value="подать"]') || document.querySelector('input[type="button"][value="редактировать"]') || document.querySelector('input[type="button"][value="создать"]');
}

function listenEvent(elem, func) {
    elem.removeEventListener('input', func);
    elem.addEventListener('input', func);
    elem.addEventListener('input', searchWord);
}
//--------------- END ----------------
//---------------------------------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** EXPENDITURE *****
//-----------------------------------------------------------------------------------------------------------------------------------------
function getExpenditureForm() {
    $.ajax({
        method : "POST",
        url : "/do/expenditure/form",
        async : false,
        cache : false,
        complete : function(data) {
            document.getElementById("content").innerHTML = data.responseText;
        }
    });
};
$("body").on('click', "table tbody tr td a[href='#deleteExpenditureModal']",  function() {
	idExpenditureBase = $(this).parent().parent().find('td:nth-child(1)').html();
    subdivisionNameBase = $(this).parent().parent().find('td:nth-child(3)').html();
});
function deleteExpenditure() {
    //1 необходимо выбрать идентификатор удаляемого подразделения
    let idExpenditure = idExpenditureBase;
    let subdivisionName = subdivisionNameBase;
    $.ajax({
        method : "POST",
        url : "/do/expenditure/delete",
        async : false,
        cache : false,
        data : "idExpenditure=" + idExpenditure,
        complete : function(response) {
            switch (response.status) {
                case (200) : {
                    getExpenditureForm();
                    //по имени подразделения мы обновляем блок info-expenditure если удаленный расход на это повлиял
                    $.ajax({
                        method : "POST",
                        url : "/do/check/expenditure/by/info",
                        async : false,
                        cache : false,
                        data : "subdivisionName=" + subdivisionName,
                        complete : function(response) {
                            if (response.status == "200") {
                                document.getElementById("info-expenditure").innerHTML = response.responseText;
                            }
                        }
                    });
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности удалить данный расход");
                    break;
                }
            }
        }
    });
};
// у каждой функции есть аргумент 'mode'
//  -> form - получить форму    
//  -> action - выполнить какие-то действия
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** EXPENDITURE STATE *****
//-----------------------------------------------------------------------------------------------------------------------------------------
function createExpenditureState(mode) {
	if (mode === "form") {
        console.log("form mode");
		//1 необходимо выбрать название подразделения
		var subdivisionName = $("#tree-subdivision ul li .selected").text();
		//2 отправляем запрос на сервер что бы получить форму
		$.ajax({
			method : "POST",
			url : "do/expenditure/state/form",
			async : false,
			cache : false,
			data : "subdivisionName=" + subdivisionName + "&" + "type=" + "add",
			complete : function(response) {
                // console.log(response);
                if (response.status == "200") {
                    document.getElementById("content").innerHTML = response.responseText;
                    checkTables();
                }
                if (response.status == "400") {
                    showNotice("ОШИБКА", response.responseText);
                }
			}
		});
	}
	if (mode === "action") {
        console.log("action mode");
        // мы должны построить обьект Expenditure и отправить его на сервер
        var expenditureState = {};
        expenditureState.idExpenditure = null;
        expenditureState.dateAndTime = null;
        expenditureState.typeExpenditure = "state";
        expenditureState.nameSubdivision = document.getElementById("content").querySelector("#nameSubdivisionForStateExpenditureModeAdd").textContent;
        expenditureState.listExpenditureCategoryCount = [];
        // теперь мы создаем список ExpenditureCategoryCount
		var arrayTable = document.getElementById("content").querySelectorAll("table");
        for (var x = 0; x < arrayTable.length; x++) { // итерируем по table
            var table = arrayTable[x];
            for (var y = 1; y < arrayTable[x].rows.length; y++) { // y = 1 потому что arrayRow[0] = th, итерируем по tr
                var expenditureCategoryCount = {};
                expenditureCategoryCount.idExpenditureCategoryCount = null;
                expenditureCategoryCount.category = {};
                expenditureCategoryCount.category.idCategory = null;
                expenditureCategoryCount.category.name = arrayTable[x].rows[y].cells[0].innerText;
                expenditureCategoryCount.category.group = null;
                expenditureCategoryCount.countCategory = arrayTable[x].rows[y].cells[1].firstElementChild.value;
                expenditureCategoryCount.listCategoryAbsenceCount = null;
                expenditureCategoryCount.expenditure = null;
                expenditureState.listExpenditureCategoryCount.push(expenditureCategoryCount);
            }
        }
        // теперь мы посылаем на сервер этот обьект expenditure тип state
        console.log(expenditureState);
        $.ajax({
           method : "POST",
            url : "/do/expenditure/state/create",
            contentType : "application/json",
            async : false,
            cache : false,
            dataType : "json",
            data : JSON.stringify(expenditureState),
            complete : function(response) {
                switch (response.status) {
                    case (200) : {
                        showNotice("УВЕДОМЛЕНИЕ", "Вы успешно создали расход по штату");
                        break;
                    }
                    case (400) : {
                        showNotice("ОШИБКА", "Нет возможности создать расход по штату");
                        break;
                    }
                }
            }
        }); 
	}
};

function deleteExpenditureState(mode) {
	if (mode === "form") {
		console.log("form mode");
        //1 необходимо выбрать название подразделения
		var subdivisionName = $("#tree-subdivision ul li .selected").text();
		//2 отправляем запрос на сервер что бы получить форму
		$.ajax({
			method : "POST",
			url : "do/expenditure/state/form",
			async : false,
			cache : false,
			data : "subdivisionName=" + subdivisionName + "&" + "type=" + "delete",
			complete : function(response) {
                if (response.status == "200") {
                    document.getElementById("content").innerHTML = response.responseText;
                }
                if (response.status == "400") {
                    showNotice("ОШИБКА", response.responseText);
                }
			}
		});
	}
	if (mode === "action") {
        console.log("action mode");
        var test = document.getElementById("nameSubdivisionForStateExpenditureModeDelete");
        console.log(test);
        $.ajax({
            method : "POST",
            url : "/do/expenditure/state/delete",
            async : false,
            cache : false,
            data : "subdivisionName=" +  document.getElementById("nameSubdivisionForStateExpenditureModeDelete").innerText,
            complete : function(response) {
                if (response.status == "200") {
                    showNotice("УВЕДОМЛЕНИЕ", "Вы успешно удалили расход ро штату");
                }
                if (response.status == "400") {
                    showNotice("ОШИБКА", "Не могу удалить расход по штату");
                }                
            }
        });
	}
};

function updateExpenditureState(mode) {
	if (mode === "form") {
		console.log("form mode");
        //1 необходимо выбрать название подразделения
		var subdivisionName = $("#tree-subdivision ul li .selected").text();
		//2 отправляем запрос на сервер что бы получить форму
		$.ajax({
			method : "POST",
			url : "do/expenditure/state/form",
			async : false,
			cache : false,
			data : "subdivisionName=" + subdivisionName + "&" + "type=" + "edit",
			complete : function(response) {
                console.log(response);
                if (response.status == "200") {
                    document.getElementById("content").innerHTML = response.responseText;
                    checkTables();
                }
                if (response.status == "400") {
                    console.log(response);
                    showNotice("ОШИБКА", response.responseText);
                }
			}
		});
	}
	if (mode === "action") {
        console.log("action mode");
        // мы должны построить обьект Expenditure и отправить его на сервер
        var expenditureState = {};
        expenditureState.idExpenditure = null;
        expenditureState.dateAndTime = null;
        expenditureState.typeExpenditure = "state";
        expenditureState.nameSubdivision = document.getElementById("content").querySelector("#nameSubdivisionForStateExpenditureModeEdit").textContent;
        expenditureState.listExpenditureCategoryCount = [];
        // теперь мы создаем список ExpenditureCategoryCount
        var arrayTable = document.getElementById("content").querySelectorAll("table");
        for (var x = 0; x < arrayTable.length; x++) { // итерируем по table
            var table = arrayTable[x];
            for (var y = 1; y < arrayTable[x].rows.length; y++) { // y = 1 потому что arrayRow[0] = th, итерируем по tr
                var expenditureCategoryCount = {};
                expenditureCategoryCount.idExpenditureCategoryCount = null;
                expenditureCategoryCount.category = {};
                expenditureCategoryCount.category.idCategory = null;
                expenditureCategoryCount.category.name = arrayTable[x].rows[y].cells[0].innerText;
                expenditureCategoryCount.category.group = null;
                expenditureCategoryCount.countCategory = arrayTable[x].rows[y].cells[1].firstElementChild.value;
                expenditureCategoryCount.listCategoryAbsenceCount = null;
                expenditureCategoryCount.expenditure = null;
                expenditureState.listExpenditureCategoryCount.push(expenditureCategoryCount);
            }
        }
        // теперь мы посылаем на сервер этот обьект expenditure тип state
        $.ajax({
           method : "POST",
            url : "/do/expenditure/state/update",
            contentType : "application/json",
            async : false,
            cache : false,
            dataType : "json",
            data : JSON.stringify(expenditureState),
            complete : function(response) {
                switch (response.status) {
                    case (200) : {
                        showNotice("УВЕДОМЛЕНИЕ", "Вы успешно обновили расход по штату");
                        break;
                    }
                    case (400) : {
                        showNotice("ОШИБКА", "Нет возможности обновить расход по штату");
                        break;
                    }
                }
            }
        }); 
	}
};
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** EXPENDITURE LIST *****
//-----------------------------------------------------------------------------------------------------------------------------------------
function createExpenditureList(mode) {
	if (mode === "form") {
        console.log("form mode");
        //1 необходимо выбрать название подразделения
		var subdivisionName = $("#tree-subdivision ul li .selected").text();
		//2 отправляем запрос на сервер что бы получить форму
		$.ajax({
			method : "POST",
			url : "do/expenditure/list/form",
			async : false,
			cache : false,
			data : "subdivisionName=" + subdivisionName + "&" + "type=" + "add",
			complete : function(response) {
                if (response.status == "200") {
                    document.getElementById("content").innerHTML = response.responseText;                    
                    checkTables();
                }
                if (response.status == "400") {
                    showNotice("ОШИБКА", response.responseText);
                }
			}
		});
    }
    if (mode === "action") {
        console.log("action mode");
        
        // мы должны построить обьект Expenditure и отправить его на сервер
        var expenditureList = {};
        expenditureList.idExpenditure = null;
        expenditureList.dateAndTime = null;
        expenditureList.typeExpenditure = "list";
        expenditureList.nameSubdivision = document.getElementById("content").querySelector("#nameSubdivisionForListExpenditureModeAdd").textContent;
        expenditureList.listExpenditureCategoryCount = [];
        // теперь мы создаем список ExpenditureCategoryCount
		var arrayTable = document.getElementById("content").querySelectorAll("table");
        for (var x = 0; x < arrayTable.length; x++) { // итерируем по table
            var table = arrayTable[x];
            for (var y = 1; y < arrayTable[x].rows.length; y++) { // y = 1 потому что arrayRow[0] = th, итерируем по tr
                var expenditureCategoryCount = {};
                expenditureCategoryCount.idExpenditureCategoryCount = null;
                expenditureCategoryCount.category = {};
                expenditureCategoryCount.category.idCategory = null;
                expenditureCategoryCount.category.name = arrayTable[x].rows[y].cells[0].innerText;
                expenditureCategoryCount.category.group = null;
                expenditureCategoryCount.countCategory = arrayTable[x].rows[y].cells[2].firstElementChild.value;
                expenditureCategoryCount.listCategoryAbsenceCount = null;
                expenditureCategoryCount.expenditure = null;
                expenditureList.listExpenditureCategoryCount.push(expenditureCategoryCount);
            }
        }
        // теперь мы посылаем на сервер этот обьект expenditure тип list
        console.log(expenditureList);
        $.ajax({
           method : "POST",
            url : "/do/expenditure/list/create",
            contentType : "application/json",
            async : false,
            cache : false,
            dataType : "json",
            data : JSON.stringify(expenditureList),
            complete : function(response) {
                switch (response.status) {
                    case (200) : {
                        showNotice("УВЕДОМЛЕНИЕ", "Вы успешно создали расход по списку");
                        break;
                    }
                    case (400) : {
                        showNotice("ОШИБКА", "Нет возможности создать расход по списку");
                        break;
                    }
                }
            }
        });
    }
};

function deleteExpenditureList(mode) {
	if (mode === "form") {
        console.log("form mode"); 
        //1 необходимо выбрать название подразделения
		var subdivisionName = $("#tree-subdivision ul li .selected").text();
		//2 отправляем запрос на сервер что бы получить форму
		$.ajax({
			method : "POST",
			url : "do/expenditure/list/form",
			async : false,
			cache : false,
			data : "subdivisionName=" + subdivisionName + "&" + "type=" + "delete",
			complete : function(response) {
                if (response.status == "200") {
                    document.getElementById("content").innerHTML = response.responseText;
                }
                if (response.status == "400") {
                    showNotice("ОШИБКА", response.responseText);
                }
			}
		});
    }
    if (mode === "action") {
        console.log("action mode");
        var test = document.getElementById("nameSubdivisionForListExpenditureModeDelete");
        console.log(test);
        $.ajax({
            method : "POST",
            url : "/do/expenditure/list/delete",
            async : false,
            cache : false,
            data : "subdivisionName=" +  document.getElementById("nameSubdivisionForListExpenditureModeDelete").innerText,
            complete : function(response) {
                if (response.status == "200") {
                    showNotice("УВЕДОМЛЕНИЕ", "Вы успешно удалили расход по списку");
                }
                if (response.status == "400") {
                    showNotice("ОШИБКА", "Не могу удалить расход по списку");
                }                
            }
        });
    }
};

function updateExpenditureList(mode) {
    if (mode === "form") {
        console.log("form mode"); 
        //1 необходимо выбрать название подразделения
		var subdivisionName = $("#tree-subdivision ul li .selected").text();
		//2 отправляем запрос на сервер что бы получить форму
		$.ajax({
			method : "POST",
			url : "do/expenditure/list/form",
			async : false,
			cache : false,
			data : "subdivisionName=" + subdivisionName + "&" + "type=" + "edit",
			complete : function(response) {
                if (response.status == "200") {
                    document.getElementById("content").innerHTML = response.responseText;                    
                    checkTables();
                }
                if (response.status == "400") {
                    showNotice("ОШИБКА", response.responseText);
                }
			}
		});
    }
    if (mode === "action") {
        console.log("action mode");
        // мы должны построить обьект Expenditure и отправить его на сервер
        var expenditureList = {};
        expenditureList.idExpenditure = null;
        expenditureList.dateAndTime = null;
        expenditureList.typeExpenditure = "list";
        expenditureList.nameSubdivision = document.getElementById("content").querySelector("#nameSubdivisionForListExpenditureModeEdit").textContent;
        expenditureList.listExpenditureCategoryCount = [];
        // теперь мы создаем список ExpenditureCategoryCount
        var arrayTable = document.getElementById("content").querySelectorAll("table");
        for (var x = 0; x < arrayTable.length; x++) { // итерируем по table
            var table = arrayTable[x];
            for (var y = 1; y < arrayTable[x].rows.length; y++) { // y = 1 потому что arrayRow[0] = th, итерируем по tr
                var expenditureCategoryCount = {};
                expenditureCategoryCount.idExpenditureCategoryCount = null;
                expenditureCategoryCount.category = {};
                expenditureCategoryCount.category.idCategory = null;
                expenditureCategoryCount.category.name = arrayTable[x].rows[y].cells[0].innerText;
                expenditureCategoryCount.category.group = null;
                expenditureCategoryCount.countCategory = arrayTable[x].rows[y].cells[2].firstElementChild.value;
                expenditureCategoryCount.listCategoryAbsenceCount = null;
                expenditureCategoryCount.expenditure = null;
                expenditureList.listExpenditureCategoryCount.push(expenditureCategoryCount);
            }
        }
        // теперь мы посылаем на сервер этот обьект expenditure тип list
        $.ajax({
           method : "POST",
            url : "/do/expenditure/list/update",
            contentType : "application/json",
            async : false,
            cache : false,
            dataType : "json",
            data : JSON.stringify(expenditureList),
            complete : function(response) {
                switch (response.status) {
                    case (200) : {
                        showNotice("УВЕДОМЛЕНИЕ", "Вы успешно обновили расход по списку");
                        break;
                    }
                    case (400) : {
                        showNotice("ОШИБКА", "Нет возможности обновить расход по списку");
                        break;
                    }
                }
            }
        });
    }
};
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** EXPENDITURE CURRENT *****
//-----------------------------------------------------------------------------------------------------------------------------------------
function postExpenditureCurrent(mode) {
	if (mode == "form") {
        console.log("form mode");
        // 1 достаем название подразделения
        var subdivisionName = $("#tree-subdivision ul li .selected").text();
        // 2 отправляем запрос на сервер для получения формы на подачу расхода
        $.ajax({
			method : "POST",
			url : "do/expenditure/current/form",
			async : false,
			cache : false,
			data : "subdivisionName=" + subdivisionName + "&" + "type=" + "post",
			complete : function(response) {
                if (response.status == "200") {
                    document.getElementById("content").innerHTML = response.responseText;
                    checkTables();
                }
                if (response.status == "400") {
                    showNotice("ОШИБКА", response.responseText);
                }
			}
		});
    }
    if (mode == "action") {
        console.log("action mode");
        // мы должны создать правильный расход "налицо" expenditure и отправить его на сервер
        var expenditureCurrent = {};
        expenditureCurrent.idExpenditure = null;
        expenditureCurrent.dateAndTime = null;
        expenditureCurrent.typeExpenditure = "current";
        expenditureCurrent.nameSubdivision = document.getElementById("content").querySelector("#nameSubdivisionForCurrentExpenditureModePost").textContent;
        expenditureCurrent.listExpenditureCategoryCount = [];
        // теперь мы создаем список ExpenditureCategoryCount
        var arrayTableCategory = document.getElementById("content").querySelectorAll(".forCategory");
        for (var x = 0; x < arrayTableCategory.length; x++) { // итерируем по tableCategory
            for (var y = 1; y < arrayTableCategory[x].rows.length; y++) { // y = 1 потому что arrayRow[0] = th, итерируем по tr
                var expenditureCategoryCount = {};
                expenditureCategoryCount.expenditure = null;
                //expenditureCategoryCount.idExpenditureCategoryCount = null;
                expenditureCategoryCount.category = {};
                //expenditureCategoryCount.category.idCategory = null;
                expenditureCategoryCount.category.name = arrayTableCategory[x].rows[y].cells[0].innerText;
                expenditureCategoryCount.category.group = null;
                expenditureCategoryCount.countCategory = arrayTableCategory[x].rows[y].cells[3].firstElementChild.value;
                expenditureCategoryCount.mapAbsenceCount = [];
                    for (var z = 0; z < arrayTableCategory[x].rows[y].cells[4].firstElementChild.rows.length; z++) {
                        var elMap = {};
                        elMap.absence = {};
                        elMap.idAbsence = null;
                        elMap.absence.name = arrayTableCategory[x].rows[y].cells[4].firstElementChild.rows[z].cells[0].innerText;
                        elMap.absence.listCategoryAbsence = null;
                        elMap.countAbsence = arrayTableCategory[x].rows[y].cells[4].firstElementChild.rows[z].cells[1].firstElementChild.value;
                        expenditureCategoryCount.mapAbsenceCount.push(elMap);
                    }
                expenditureCurrent.listExpenditureCategoryCount.push(expenditureCategoryCount);
            }
        }
        // теперь мы посылаем на сервер этот обьект expenditure тип "налицо"
        $.ajax({
           method : "POST",
            url : "/do/expenditure/current/create",
            contentType : "application/json",
            async : false,
            cache : false,
            dataType : "json",
            data : JSON.stringify(expenditureCurrent),
            complete : function(response) {
                switch (response.status) {
                    case (200) : {
                        showNotice("УВЕДОМЛЕНИЕ", "Вы успешно подали расход");
                        break;
                    }
                    case (400) : {
                        showNotice("ОШИБКА", "Нет возможности подать расход");
                        break;
                    }
                }
            }
        });
    }
};

function updateExpenditureCurrent(mode) {
	if (mode == "form") {
        console.log("form mode");
        // 1 достаем название подразделения
        var subdivisionName = $("#tree-subdivision ul li .selected").text();
        // 2 отправляем запрос на сервер для получения формы на подачу расхода
        $.ajax({
			method : "POST",
			url : "do/expenditure/current/form",
			async : false,
			cache : false,
			data : "subdivisionName=" + subdivisionName + "&" + "type=" + "edit",
			complete : function(response) {
                if (response.status == "200") {
                    document.getElementById("content").innerHTML = response.responseText;
                    checkTables();
                }
                if (response.status == "400") {
                    showNotice("ОШИБКА", response.responseText);
                }
			}
		});
    }
    if (mode == "action") {
        console.log("action mode");
        console.log(document.getElementById("content").querySelector("#nameSubdivisionForCurrentExpenditureModeEdit"));
        // мы должны создать правильный расход "налицо" expenditure и отправить его на сервер
        var expenditureCurrent = {};
        expenditureCurrent.idExpenditure = null;
        expenditureCurrent.dateAndTime = null;
        expenditureCurrent.typeExpenditure = "current";
        expenditureCurrent.nameSubdivision = document.getElementById("content").querySelector("#nameSubdivisionForCurrentExpenditureModeEdit").textContent;
        expenditureCurrent.listExpenditureCategoryCount = [];
        // теперь мы создаем список ExpenditureCategoryCount
        var arrayTableCategory = document.getElementById("content").querySelectorAll(".forCategory");
        for (var x = 0; x < arrayTableCategory.length; x++) { // итерируем по tableCategory
            for (var y = 1; y < arrayTableCategory[x].rows.length; y++) { // y = 1 потому что arrayRow[0] = th, итерируем по tr
                var expenditureCategoryCount = {};
                expenditureCategoryCount.expenditure = null;
                //expenditureCategoryCount.idExpenditureCategoryCount = null;
                expenditureCategoryCount.category = {};
                //expenditureCategoryCount.category.idCategory = null;
                expenditureCategoryCount.category.name = arrayTableCategory[x].rows[y].cells[0].innerText;
                expenditureCategoryCount.category.group = null;
                expenditureCategoryCount.countCategory = arrayTableCategory[x].rows[y].cells[3].firstElementChild.value;
                expenditureCategoryCount.mapAbsenceCount = [];
                    for (var z = 0; z < arrayTableCategory[x].rows[y].cells[4].firstElementChild.rows.length; z++) {
                        var elMap = {};
                        elMap.absence = {};
                        elMap.idAbsence = null;
                        elMap.absence.name = arrayTableCategory[x].rows[y].cells[4].firstElementChild.rows[z].cells[0].innerText;
                        elMap.absence.listCategoryAbsence = null;
                        elMap.countAbsence = arrayTableCategory[x].rows[y].cells[4].firstElementChild.rows[z].cells[1].firstElementChild.value;
                        expenditureCategoryCount.mapAbsenceCount.push(elMap);
                    }
                expenditureCurrent.listExpenditureCategoryCount.push(expenditureCategoryCount);
            }
        }
        // теперь мы посылаем на сервер этот обьект expenditure тип "налицо"
        $.ajax({
           method : "POST",
            url : "/do/expenditure/current/update",
            contentType : "application/json",
            async : false,
            cache : false,
            dataType : "json",
            data : JSON.stringify(expenditureCurrent),
            complete : function(response) {
                switch (response.status) {
                    case (200) : {
                        showNotice("УВЕДОМЛЕНИЕ", "Вы успешно обновили расход");
                        break;
                    }
                    case (400) : {
                        showNotice("ОШИБКА", "Нет возможности обновить расход");
                        break;
                    }
                }
            }
        });
    }
};

function showExpenditureCurrent(mode) {
	if (mode == "form") {
        console.log("form mode");
        // 1 достаем название подразделения
        var subdivisionName = $("#tree-subdivision ul li .selected").text();
        $.ajax({
			method : "POST",
			url : "do/expenditure/current/form",
			async : false,
			cache : false,
			data : "subdivisionName=" + subdivisionName + "&" + "type=" + "show",
			complete : function(response) {
                if (response.status == "200") {
                    document.getElementById("content").innerHTML = response.responseText;
                }
                if (response.status == "400") {
                    showNotice("ОШИБКА", response.responseText);
                }
			}
		});
    }
    if (mode == "action") {
        
    }
};
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** EXPENDITURE SUM *****
//-----------------------------------------------------------------------------------------------------------------------------------------
function getSumExpenditure() {
    $.ajax({
        method : "POST",
        url : "do/expenditure/sum/form",
        async : false,
        cache : false,
        complete : function(response) {
            if (response.status == "200") {
                document.getElementById("content").innerHTML = response.responseText;
            }
            if (response.status == "400") {
                showNotice("ОШИБКА", response.responseText);
            }
        }
    });
};
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** TITLE *****
//-----------------------------------------------------------------------------------------------------------------------------------------
$("body").on('click', "table tbody tr td a[href='#deleteTitleModal']",  function() {
	titleModal.idTitle = $(this).parent().parent().find('td:nth-child(1)').html();
    titleModal.name = $(this).parent().parent().find('td:nth-child(2)').html();
});

$("body").on('click', "table tbody tr td a[href='#editTitleModal']",  function() {
	titleModal.idTitle = $(this).parent().parent().find('td:nth-child(1)').html();
    titleModal.name = $(this).parent().parent().find('td:nth-child(2)').html();
    $('#editNameTitle').val(titleModal.name);
});

function getTitleForm() {
    $.ajax({
        method : "POST",
        url : "/do/title/form",
        async : false,
        cache : false,
        complete : function(data) {
            document.getElementById("content").innerHTML = data.responseText;
        }
    });
};

function addTitle() {

	titleModal.idTitle = null;
	titleModal.name = $('#nameTitle').val();
	
    $.ajax({
       method : "POST",
        url : "/do/title/add",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(titleModal),
        complete : function(response) {
            switch (response.status) {
                case (200) : {
                    getTitleForm();
                    //
                    //document.getElementById("content").innerHTML = response.;
                    //console.log("into response");
                    //console.log(response);
                    break;
                }
                case (400) : {
                    showNotice("ОШИБКА", "Нет возможности добавить новую группу");
                    break;
                }
            }
        }
    });
};

function deleteTitle() {
    $.ajax({
        method : "POST",
        url : "/do/title/delete",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(titleModal),
        complete : function(response) {
            switch (response.status) {
                case (200) : {
                    getTitleForm();
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности удалить данную группу");
                    break;
                }
            }
        }
    });
};

function updateTitle() {
	
	titleModal.name = $('#editNameTitle').val();
	
    $.ajax({
        method : "POST",
        url : "/do/title/update",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(titleModal),
        complete : function(e, xhr, settings) {
            switch (e.status) {
                case (200) : {
                    getTitleForm();
                    
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности обновить данную группу");
                    break;
                }
            }
        }
    });
};
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** CATEGORY *****
//-----------------------------------------------------------------------------------------------------------------------------------------
$("body").on('click', "table tbody tr td a[href='#deleteCategoryModal']",  function() {
	categoryModal.idCategory = $(this).parent().parent().find('td:nth-child(1)').html();
    categoryModal.name = $(this).parent().parent().find('td:nth-child(2)').html();
    categoryModal.title.name = $(this).parent().parent().find('td:nth-child(3)').html();
});

$("body").on('click', "table tbody tr td a[href='#editCategoryModal']",  function() {
	categoryModal.idCategory = $(this).parent().parent().find('td:nth-child(1)').html();
    categoryModal.name = $(this).parent().parent().find('td:nth-child(2)').html();
    categoryModal.title.name = $(this).parent().parent().find('td:nth-child(3)').html();
    $('#editNameCategory').val(categoryModal.name);
    $('#editNameTitle').val(categoryModal.title.name);
});

function getCategoryForm() {	
    $.ajax({
        method : "POST",
        url : "/do/category/form",
        async : false,
        cache : false,
        complete : function(data) {
			document.getElementById("content").innerHTML = data.responseText;
        }
    });	
};

function addCategory() {

    categoryModal.name = $('#nameCategory').val();
    categoryModal.title.name = $('#nameTitle').val();

    $.ajax({
        method : "POST",
        url : "/do/category/add",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(categoryModal),
        complete : function(response) {
			switch (response.status) {
                case (200) : {
                    getCategoryForm();
                    break;
                }
                case (400) : {
                    showNotice("ОШИБКА", "Нет возможности добавить новую категорию");
                    break;
                }
            }
        }
    });
};

function deleteCategory() {
    $.ajax({
        method : "POST",
        url : "/do/category/delete",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(categoryModal),
        complete : function(response) {
            switch (response.status) {
                case (200) : {
                    getCategoryForm();
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности удалить данную категорию");
                    break;
                }
            }
        }
    });
};

function updateCategory() {
	
	categoryModal.name = $("#editNameCategory").val();
	
    $.ajax({
        method : "POST",
        url : "/do/category/update",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(categoryModal),
        complete : function(response) {
			switch (response.status) {
                case (200) : {
                    getCategoryForm();
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности обновить данную категорию");
                    break;
                }
            }
        }
    });	
};
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** ABSENCE *****
//-----------------------------------------------------------------------------------------------------------------------------------------
function bindCategory() {
    var category = $('#nameCategoryForAbsence').val();
	
	//если такая категория уже есть в списке, то нельзя её ложить в этот список
	var listCategory = document.getElementById("listCategory");
	console.log(listCategory.childElementCount);
	if (listCategory.childElementCount == 0) {
		listCategory.innerHTML = "<option>" + category + "</option>";
		return;
	}
	
	var array = listCategory.childNodes;
	
	//проверяет есть ли элемент с таким именем, если есть то выходит из функции
	for (var i = 0; i < array.length; i++) {
		if (category === array[i].innerHTML) {
			return;
		}
	}
	
	var child = document.createElement("option");
	var contentOfChild = document.createTextNode(category);
	child.appendChild(contentOfChild);
	listCategory.append(child);
};

$("body").on('click', "table tbody tr td a[href='#deleteAbsenceModal']",  function() {
	absenceModal.idAbsence = $(this).parent().parent().find('td:nth-child(1)').html();
    absenceModal.name = $(this).parent().parent().find('td:nth-child(2)').html();
});

function getAbsenceForm() {
    $.ajax({
        method : "POST",
        url : "/do/absence/form",
        async : false,
        cache : false,
        complete : function(data) {
			document.getElementById("content").innerHTML = data.responseText;
        }
    });	
};

function addAbsence() {

	absenceModal.name = $('#nameAbsence').val();
	absenceModal.listCategoryAbsence = [];
	
	$('#listCategory > option').each(function() {
		var categoryModal = {};
		categoryModal.idCategory = null;
		categoryModal.name = $(this).text();
		categoryModal.group = null;
		
		var categoryAbsence = {};
		categoryAbsence.idCategoryAbsence = null;
		categoryAbsence.absence = null;
		categoryAbsence.category = categoryModal;
		
		absenceModal.listCategoryAbsence.push(categoryAbsence); 
    });

    $.ajax({
        method : "POST",
        url : "/do/absence/add",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(absenceModal),
        complete : function(response) {
			switch (response.status) {
                case (200) : {
                    getAbsenceForm();
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности добавить данную причичну отсутствия");
                    break;
                }
            }
        }
    });
};

function deleteAbsence() {
    $.ajax({
        method : "POST",
        url : "/do/absence/delete",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(absenceModal),
        complete : function(response) {
			switch (response.status) {
                case (200) : {
                    getAbsenceForm();
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности удалить данную причину отсутствия");
                    break;
                }
            }
        }
    });
};
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** SUBDIVISION *****
//-----------------------------------------------------------------------------------------------------------------------------------------
$("body").on("click", "table tbody tr td a[href='#deleteSubdivisionModal']",  function() {
	subdivisionModal.idSubdivision = $(this).parent().parent().find('td:nth-child(1)').html();
    subdivisionModal.name = $(this).parent().parent().find('td:nth-child(2)').html();
	subdivisionModal.parent = null;
});

$("body").on("click", "table tbody tr td a[href='#editSubdivisionModal']",  function() {
	subdivisionModal.idSubdivision = $(this).parent().parent().find('td:nth-child(1)').html();
    subdivisionModal.name = $(this).parent().parent().find('td:nth-child(2)').html();
	subdivisionModal.parent = {};
	subdivisionModal.parent.idSubdivision = null;
	subdivisionModal.parent.name = $(this).parent().parent().find('td:nth-child(3)').html();
	subdivisionModal.parent.parent = null;

    $('#editNameSubdivision').val(subdivisionModal.name);
    var nameSubdivision = document.getElementById("editNameSubdivision");   
    nameSubdivision.innerHTML = subdivisionModal.name;
    
	if (subdivisionModal.parent.name == "") {
		$('#editNameParentSubdivision').val("root");
	} else {
		$('#editNameParentSubdivision').val(subdivisionModal.parent.name);
	}
});

//'editSubdivisionModal' когда появляеться прятать элемент подразделения display: none
// что бы нельзя было назначить родителем самого себя
$(document).on('show.bs.modal', '#editSubdivisionModal',  function (event) {
    console.log("shown editSubdivision");
    var nameSubdivision = $(this).find("#editNameSubdivision").val();

    $(this).find("option").each(function() {
        console.log(this);
        if ($(this).val() === nameSubdivision) {
            $(this).css('display', 'none');
        }
    });
});

//'editSubdivisionModal' когда прячется снова его показывать
$(document).on('hide.bs.modal', '#editSubdivisionModal',  function (event) {
    console.log("hidden editSubdivision");
    console.log("shown editSubdivision");
    var nameSubdivision = $(this).find("#editNameSubdivision").val();

    $(this).find("option").each(function() {
        console.log(this);
        if ($(this).val() === nameSubdivision) {
            $(this).css('display', '');
        }
    });
});

function getSubdivisionForm() {
    $.ajax({
        method : "POST",
        url : "/do/subdivision/form",
        async : false,
        cache : false,
        complete : function(data) {
			document.getElementById("content").innerHTML = data.responseText;
            $("#tree-subdivision").find("ul:nth-child(1)").css("padding", 0);
        }
    });
};

function addSubdivision() {

	subdivisionModal.idSubdivision = null;
	subdivisionModal.name = $("#nameSubdivision").val();
	subdivisionModal.parent = {};
	subdivisionModal.parent.idSubdivison = null;
	subdivisionModal.parent.name = $("#subdivisionParentName").val();
	subdivisionModal.parent.parent = null;

    $.ajax({
        method : "POST",
        url : "/do/subdivision/add",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(subdivisionModal),
        complete : function(response) {
            switch (response.status) {
                case (200) : {
					getSubdivisionForm();
					$.ajax({
						method : "POST",
						url : "/do/subdivision/tree",
						async : false,
						cache : false,
						complete : function(data) {
							document.getElementById("tree-subdivision").innerHTML = data.responseText;
                            $("#tree-subdivision").find("ul:nth-child(1)").css("padding", 0);
						}
					});
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности добавить данное подразделение");
                    break;
                }
            }
        }
    });
};

function deleteSubdivision() {
    $.ajax({
        method : "POST",
        url : "/do/subdivision/delete",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(subdivisionModal),
        complete : function(response) {
            switch (response.status) {
                case (200) : {
					getSubdivisionForm();
					$.ajax({
						method : "POST",
						url : "/do/subdivision/tree",
						async : false,
						cache : false,
						complete : function(data) {
							document.getElementById("tree-subdivision").innerHTML = data.responseText;
                            $("#tree-subdivision").find("ul:nth-child(1)").css("padding", 0);
						}
					});
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности удалить данное подразделение");
                    break;
                }
            }
        }
    });
};

function updateSubdivision() {
	
	subdivisionModal.name = $("#editNameSubdivision").val();
	subdivisionModal.parent.name = $("#editNameParentSubdivision").val();
	
    $.ajax({
        method : "POST",
        url : "/do/subdivision/update",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(subdivisionModal),
        complete : function(response) {
            switch (response.status) {
                case (200) : {
					getSubdivisionForm();
					$.ajax({
						method : "POST",
						url : "/do/subdivision/tree",
						async : false,
						cache : false,
						complete : function(data) {
							document.getElementById("tree-subdivision").innerHTML = data.responseText;
                            $("#tree-subdivision").find("ul:nth-child(1)").css("padding", 0);
						}
					});
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности обновить данное подразделение");
                    break;
                }
            }
        }
    });
};

function bindParentSubdivision() {
    var nameParentForSubdivision = $('#nameParentForSubdivision').val();
    $('#editNameParentSubdivision').val(nameParentForSubdivision);
};

$("body").on('click', "table tbody tr td a[href='#deleteExpenditureModal']",  function() {
    idDeleteExpenditure = $(this).parent().parent().find('td:nth-child(1)').html();
});
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** USER *****
//-----------------------------------------------------------------------------------------------------------------------------------------
$("body").on('click', "table tbody tr td a[href='#deleteUserModal']",  function() {
    userModal.name = $(this).parent().parent().find('td:nth-child(2)').html();
});

$("body").on('click', "table tbody tr td a[href='#editUserModal']",  function() {
	userModal.idUser = $(this).parent().parent().find('td:nth-child(1)').html();
    userModal.name = $(this).parent().parent().find('td:nth-child(2)').html();
	userModal.password = $(this).parent().parent().find('td:nth-child(3)').html();
	userModal.roleSystem = $(this).parent().parent().find('td:nth-child(4)').html();
	
	var root = document.getElementById("setOnListUserSubdivisionEdit");
	root.innerHTML = "";
	
	var array = [];
	$.each($(this).parent().parent().find('td:nth-child(5) div').find("div.itemUserSubdivision"), function(index, value) {
		var element = $(value).clone();
		array.push(element);
	});
	
	$.each(array, function(index, value) {
		$(value).find("div").attr("style", "");
	});
	
	var root = document.getElementById("setOnListUserSubdivisionEdit");
	for (var i = 0; i < array.length; i++) {
		var item = array[i];
		root.appendChild(item[0]);
	}
	
	$("#setOnListUserSubdivisionEdit").find("div.itemUserSubdivision").attr("onclick", "markAsDeleted(this)");
	
	$('#nameUserEdit').val(userModal.name);
	$('#passwordUserEdit').val(userModal.password);
	$('#systemRoleUserEdit').val(userModal.roleSystem);
});

// обвести красным удаляемую запись
function markAsDeleted(element) {
	if (element.classList.contains("bg-danger") == true) {
		element.classList.remove("bg-danger");
	} else {
		element.classList.add("bg-danger");
	}
};

// создать новую запись user_subdivision
function createUserSubdivision(type) {
	var str = "#setOnListUserSubdivision" + type;
	
	var nameSubdivisionAll = false;
	
	// уже есть элемент с названием 'все'
	$.each($(str).find('div.itemUserSubdivision'), function(index, object) {
		var nameSubdivision = $(object).find(".userSubdivisionNameSubdivision").val();
		if (nameSubdivision === "все") {
			nameSubdivisionAll = true;
			console.log("1.1");
		}
	});
	
	if (nameSubdivisionAll) {
		//если присутствует элемент с параметром 'все' то не добавлять ничего
		console.log("1");
		return;
	}
	
	var nameSubdivisionIsEqual = false;
	
	var str2 = "#listSubdivisionModal" + type;
	var nameSubdivision = $(str2).val();
	console.log(nameSubdivision);
	var forLength = str + " div.itemUserSubdivision";
	console.log(forLength);
	console.log($(forLength).length);
	
	if ((nameSubdivision === "все") && ($(forLength).length !== 0)) {
		nameSubdivisionAll = true;
		console.log("2.2");	
	}
	
	if (nameSubdivisionAll) {
		//если присутствует элемент с параметром 'все' то не добавлять ничего
		console.log("2");
		return;
	}
	
	$.each($(str).find('div.itemUserSubdivision'), function(index, object) {
		var nameSubdivisionInLoop = $(object).find(".userSubdivisionNameSubdivision").val();
		if (nameSubdivisionInLoop === nameSubdivision) {
			nameSubdivisionIsEqual = true;
			console.log("3.3");
		}
	});

	if (nameSubdivisionIsEqual) {
		//если присутствует элемент с таким подразделением то не добавлять его
		console.log("3");
		return;
	}
	
	var str3 = "#listIsEditModal" + type;
	var isEditUser = $(str3).val();
	var itemUserSubdivision =	"<div onclick='markAsDeleted(this)' class='itemUserSubdivision input-group my-1 d-flex justify-content-center'>" +
									"<div class='input-group-prepend m-1'>" +
										"<input readonly class='form-control userSubdivisionId' type='text' value=0 />" +
									"</div>" +
									"<div class='input-group-prepend m-1'>" +
										"<input readonly class='form-control userSubdivisionNameSubdivision' type='text' value='" + nameSubdivision + "'/>" +
									"</div>" +
									"<div class='input-group-append m-1'>" +
										"<input readonly class='form-control userSubdivisionIsEdit' type='text' value='" + isEditUser + "'/>" +
									"</div>" +
								"</div>";
	$(str).append(itemUserSubdivision);
};

function deleteUserSubdivision(type) {
	var str = "setOnListUserSubdivision" + type;
	var root = document.getElementById(str);
	var elements = root.querySelectorAll(".bg-danger");
	elements.forEach(function(node) {
		node.remove();
	});
};

function getUserForm() {
	$.ajax({
		method : "POST",
		url : "/do/user/form",
		async : false,
		cache : false,
		complete : function(data) {
			document.getElementById("content").innerHTML = data.responseText;
		}
    });	
};

function addUser() {

	userModal.idUser = null;
	userModal.name = $('#nameUser').val();
	userModal.password = $('#passwordUser').val();
	userModal.roleSystem = $('#systemRoleUser').val();
	userModal.listUserSubdivision = [];
		
	$.each($('#setOnListUserSubdivision').find('div.itemUserSubdivision'), function(index, object) {
		var nameSubdivision = $(object).find(".userSubdivisionNameSubdivision").val();
		var isEditAsText = $(object).find(".userSubdivisionIsEdit").val();
		var isEdit = null;
		if (isEditAsText === 'редактирование') {
			isEdit = true;
		}
		if (isEditAsText === 'нет') {
			isEdit = false;
		}
		
		var userSubdivision = {};
		userSubdivision.user = null;
		userSubdivision.subdivision = {};
		userSubdivision.subdivision.name = nameSubdivision;
		userSubdivision.isEdit = isEdit;
		userModal.listUserSubdivision.push(userSubdivision);
	});
	
	$.ajax({
		method : "POST",
		url : "/do/user/add",
		contentType : "application/json",
		async : false,
		cache : false,
		dataType : "json",
		data : JSON.stringify(userModal),
		complete : function(response) {
            switch (response.status) {
                case (200) : {
					getUserForm();
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности добавить данного пользователя");
                    break;
                }
            }			
		}
    });	
};

function deleteUser() {
	$.ajax({
        method : "POST",
        url : "/do/user/delete",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(userModal),
        complete : function(response) {
            switch (response.status) {
                case (200) : {
					getUserForm();
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности удалить данного пользователя");
                    break;
                }
            }				
        }
    });
};

function updateUser() {
	userModal.name = $("#nameUserEdit").val();
	userModal.password = $("#passwordUserEdit").val();
	userModal.roleSystem = $("#systemRoleUserEdit").val();
	userModal.listUserSubdivision = [];
	
	$.each($('#setOnListUserSubdivisionEdit').find('div.itemUserSubdivision'), function(index, object) {
		var nameSubdivision = $(object).find(".userSubdivisionNameSubdivision").val();
		var isEditAsText = $(object).find(".userSubdivisionIsEdit").val();
		var isEdit = null;
		if (isEditAsText === 'редактирование') {
			isEdit = true;
		}
		if (isEditAsText === 'нет') {
			isEdit = false;
		}
		
		var userSubdivision = {};
		userSubdivision.user = null;
		userSubdivision.subdivision = {};
		userSubdivision.subdivision.name = nameSubdivision;
		userSubdivision.isEdit = isEdit;
		userModal.listUserSubdivision.push(userSubdivision);
	});
	
	$.ajax({
        method : "POST",
        url : "/do/user/update",
        contentType : "application/json",
        async : false,
        cache : false,
        dataType : "json",
        data : JSON.stringify(userModal),
        complete : function(response) {
            switch (response.status) {
                case (200) : {
					getUserForm();
                    break;
                }
                case (400) : {
					showNotice("ОШИБКА", "Нет возможности обновить данного пользователя");
                    break;
                }
            }				
        }
    });
}
//-----------------------------------------------------------------------------------------------------------------------------------------
//															***** CHECK EXPENDITURE *****
//-----------------------------------------------------------------------------------------------------------------------------------------
function checkExpenditureByPost() {
	$.ajax({
		method : "POST",
		url : "/do/check/expenditure/by/post",
		async : false,
		cache : false,
        complete : function(data) {
            document.getElementById("content").innerHTML = data.responseText;
        }
    });
}

