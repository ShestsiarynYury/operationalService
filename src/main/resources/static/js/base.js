$("#base").on("load", function() {
	$.ajax({
        method : "POST",
        url : "/do/subdivision/tree",
        async : false,
        cache : false,
        success : function(data) {
            if (data == null || data == "") {}
            else {
                $("#tree-subdivision").html(data);
                $("#tree-subdivision").find("ul:nth-child(1)").css("padding", 0);
            }
        }
    });
});

$(document).on("click", "#tree-subdivision ul li label", function(event) {
	$("#tree-subdivision ul li label").each( function() {
		$(this).css("font-weight", "");
		$(this).removeClass("selected");
	});
	$(this).css("font-weight", "bold");
	$(this).addClass("selected");
    
    getInfoExpenditure();
});
// сворачиваем и разворачиваем подразделения
$(document).on("dblclick", "#tree-subdivision ul li label", function(event) {
    if ($(this).text() !== '+') {
        if ($(this).parent().children("ul").is("ul")) {
            var plus = "<label class='plus'>+</label>";
            if ($(this).hasClass("hidden")) {
                $(this).removeClass("hidden");
                $(this).parent().children("label[class='plus']").remove();
                $(this).parent().children("ul").toggle();
            } else {
                $(this).addClass("hidden");
                $(this).parent().prepend(plus);
                $(this).parent().children("ul").toggle();
            }
        } else {

        }   
    }
});

function getInfoExpenditure() {
    // 1 достаем название подразделения
    var subdivisionName = $("#tree-subdivision ul li .selected").text();
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
};

function clearContent() {
	$("#content").html("");
};

$(document).on('hidden.bs.modal', '.fade',  function (event) {
	$(this).find('form').trigger("reset");
	$('#listCategory').html('');
	$('#setOnListUserSubdivision').html('');

});

// функция выхода из web-сервиса
function exit() {
    $.ajax({
        method : "POST",
        url : "/logout",
        async : false,
        cache : false,
        complete : function(response) {
            switch (response.status) {
                case (200) : {
                    location.reload(true);
                }
                case (400) : {

                }
            }
        }
    });
};
