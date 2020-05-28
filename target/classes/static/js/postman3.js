$(document).on('change','#file1',function () {
    var file = $("#file1").val();

    /*获取文件名部分*/
    // var strFileName=file.replace(/^.+?\\([^\\]+?)(\.[^\.\\]*?)?$/gi,"$1");  //正则表达式获取文件名，不带后缀
    // var FileExt="."+file.replace(/.+\./,"");   //正则表达式获取后缀
    $("#Key1").val($("#completeserviceName").val()+"/"+$("#completePath").val()+"/"+$("#theCompleteFileName").val());
});

$(document).on('change','#file2',function () {
    var file = $("#file2").val();

    /*获取文件名部分*/
    // var strFileName=file.replace(/^.+?\\([^\\]+?)(\.[^\.\\]*?)?$/gi,"$1");  //正则表达式获取文件名，不带后缀
    // var FileExt="."+file.replace(/.+\./,"");   //正则表达式获取后缀
    $("#Key2").val($("#completeserviceName").val()+"/"+$("#completePath").val()+"/"+$("#theCompleteFileName").val());

});

$(document).on('change','#file3',function () {
    var file = $("#file3").val();

    /*获取文件名部分*/
    // var strFileName=file.replace(/^.+?\\([^\\]+?)(\.[^\.\\]*?)?$/gi,"$1");  //正则表达式获取文件名，不带后缀
    // var FileExt="."+file.replace(/.+\./,"");   //正则表达式获取后缀
    $("#Key3").val($("#completeserviceName").val()+"/"+$("#completePath").val()+"/"+$("#theCompleteFileName").val());

});
$(document).on('change','#file4',function () {
    var file = $("#file4").val();

    /*获取文件名部分*/
    // var strFileName=file.replace(/^.+?\\([^\\]+?)(\.[^\.\\]*?)?$/gi,"$1");  //正则表达式获取文件名，不带后缀
    // var FileExt="."+file.replace(/.+\./,"");   //正则表达式获取后缀
    $("#Key4").val($("#completeserviceName").val()+"/"+$("#completePath").val()+"/"+$("#theCompleteFileName").val());

});



$(function () {

    $("#theCompleteFile").change(function () {
        var name=$("#theCompleteFileName").val();
        var strFileName=name.substring(0,name.lastIndexOf("."));
        var FileExt=name.substring(name.indexOf("."),name.length);
        $("input[name='Key']").each(function () {
            $(this).val($("#completeserviceName").val()+"/"+$("#completePath").val()+"/"+$("#theCompleteFileName").val());
        });
        $("#completeName").val(strFileName);
        $("#completeext").val(FileExt);
    });

    $("#ready").click(function(){
        var name=$("#theCompleteFileName").val();
        var strFileName=name.substring(0,name.lastIndexOf("."));//获取文件名
        var FileExt=name.substring(name.indexOf("."),name.length);//获取文件后缀名
        $("input[name='Key']").each(function () {
            $(this).val($("#completeserviceName").val()+"/"+$("#completePath").val()+"/"+$("#theCompleteFileName").val());
        });
        $("#completeName").val(strFileName);
        $("#completeext").val(FileExt);
    });

    $("#selMethod").change(function () {
        var url=$("#httpHeader").val()+$("#selMethod").val();
        $("#displayUrl").val(url);
    });

    //上传分片
    $("#btnRequest").click(function () {
        var url=$("#httpHeader").val()+$("#selMethod").val()+"?t="+ parseInt(Math.random() * 100000);
        alert(url);
        $("form[name='dataForm']").each(function () {
                var dataForm=new FormData($(this)[0]);
                doAjax(dataForm,url);
        });
    });

    function doAjax(dataForm,url) {
        var eStr="";
        $.ajax({
            type: "POST",
            url: url,
            data: dataForm,
            contentType: false,
            processData: false,
            // async:false,
            complete: function (e, xhr, settings) {
                var result = JSON.parse(e.responseText);
                eStr+=e.responseText+",";
                $("#txtResponseBody").val(eStr);
                if (e.status === 200) {
                    document.getElementById("uploadId").value=result.uploadId;
                    document.getElementById("sp"+result.partNumber).innerHTML="上传成功";
                    document.getElementById("partEtag"+result.partNumber).value=JSON.stringify(result.partETag);
                } else {
                    alert(e.status);
                }
            }
        });
    }

    //整合分片
    $("#completeUpload").click(function () {
        var url=$("#httpHeader").val()+$("#selMethod").val();
        var eStr="";
        var dataForm=new FormData($("form[name='dataFormComplete']")[0]);
        $.ajax({
            type: "POST",
            url: url,
            data: dataForm,
            contentType: false,
            processData: false,
            complete: function (e, xhr, settings) {
                var result = JSON.parse(e.responseText);
                eStr+=e.responseText+",";
                $("#txtResponseBody").val(eStr);
                if (e.status === 200) {
                    alert(result.filePath);
                    document.getElementById("completeView").href=result.filePath;
                    document.getElementById("completeView").innerHTML = result.filePath+"";
                    document.getElementById("success").innerHTML="请求成功,文件为：";
                } else {
                    alert(e.status);
                }
            }
        });
    });

    //获取分片uploadId
    $("#createUploadId").click(function(){
        var url=$("#httpHeader").val()+$("#selMethod").val();
        var key=$("#completeserviceName").val()+"/"+$("#completePath").val()+"/"+$("#theCompleteFileName").val();
        $.post(url,{key:key},function(data){
            for(var i=1;i<=Number($("#partCount"));i++){
                $("#fileId"+i).val(data);
                $("#spid"+i).html("有值");
            }
            $("#uploadId").val(data);
            $("#sp_UploadId").html("请求成功");
        });
    });

    //取消上传
    $("#cancelRequest").click(function(){
        var url=$("#httpHeader").val()+$("#selMethod").val();
        var uploadId=$("#uploadId").val();
        var key=$("#completeserviceName").val()+"/"+$("#completePath").val()+"/"+$("#theCompleteFileName").val();
        $.post(url,
            {
                key:key,
                uploadId:uploadId
            },
            function(data){
            $("#sp_cancel").html("请求成功");
        });
    });
});