//														***** global objects *****

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
    //elem.removeEventListener('click', func);
    //elem.removeEventListener('keypress', func);
    //elem.removeEventListener('keydown', func);
    elem.removeEventListener('input', func);
    //elem.removeEventListener('change', func);
    
    //elem.addEventListener('click', func);
    //elem.addEventListener('keypress', func);
    //elem.addEventListener('keydown', func);
    elem.addEventListener('input', func);
    elem.addEventListener('input', searchWord);
    //elem.addEventListener('change', func);
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
