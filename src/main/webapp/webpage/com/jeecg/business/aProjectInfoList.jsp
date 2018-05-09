<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<t:base type="jquery,easyui,tools,DatePicker"></t:base>
<div class="easyui-layout" fit="true">
  <div region="center" style="padding:0px;border:0px">
  <t:datagrid name="aProjectInfoList" checkbox="true" pagination="true" fitColumns="true" title="项目基本信息" actionUrl="aProjectInfoController.do?datagrid" idField="id" fit="true" queryMode="group">
   <t:dgCol title="主键"  field="id"  hidden="true"  queryMode="group"  width="120"></t:dgCol>
   <t:dgCol title="项目id"  field="projectId"  queryMode="group"  width="120"></t:dgCol>
   <t:dgCol title="项目名称"  field="projectName"  queryMode="group"  width="120"></t:dgCol>
  <%-- <t:dgCol title="createTime"  field="createTime"  formatter="yyyy-MM-dd"  queryMode="group"  width="120"></t:dgCol>
   <t:dgCol title="updateTime"  field="updateTime"  formatter="yyyy-MM-dd"  queryMode="group"  width="120"></t:dgCol>
   <t:dgCol title="delFlag"  field="delFlag"  queryMode="group"  width="120"></t:dgCol>--%>
   <t:dgCol title="项目简介"  field="projectIntro"  queryMode="group"  width="120"></t:dgCol>
   <t:dgCol title="remark"  field="remark"  queryMode="group"  width="120"></t:dgCol>
   <t:dgCol title="操作" field="opt" width="100"></t:dgCol>

   <t:dgFunOpt funname="accept(id,tableName)" title="预受理"  urlclass="ace_button"  urlfont="fa-copy"></t:dgFunOpt>
  <%-- <t:dgDelOpt title="删除" url="aProjectInfoController.do?doDel&id={id}" urlclass="ace_button"  urlfont="fa-trash-o"/>
   <t:dgToolBar title="录入" icon="icon-add" url="aProjectInfoController.do?goAdd" funname="add"></t:dgToolBar>
   <t:dgToolBar title="编辑" icon="icon-edit" url="aProjectInfoController.do?goUpdate" funname="update"></t:dgToolBar>
   <t:dgToolBar title="批量删除"  icon="icon-remove" url="aProjectInfoController.do?doBatchDel" funname="deleteALLSelect"></t:dgToolBar>
   <t:dgToolBar title="查看" icon="icon-search" url="aProjectInfoController.do?goUpdate" funname="detail"></t:dgToolBar>
   <t:dgToolBar title="导入" icon="icon-put" funname="ImportXls"></t:dgToolBar>
   <t:dgToolBar title="导出" icon="icon-putout" funname="ExportXls"></t:dgToolBar>
   <t:dgToolBar title="模板下载" icon="icon-putout" funname="ExportXlsByT"></t:dgToolBar>--%>
  </t:datagrid>
  </div>
 </div>
 <script src = "webpage/com/jeecg/business/aProjectInfoList.js"></script>		
 <script type="text/javascript">
 $(document).ready(function(){
 });


//预受理
function accept(id,tableName){
    createwindow("预受理", "bProjectBusinessController.do?accept&id="+id);

}
//导入
function ImportXls() {
	openuploadwin('Excel导入', 'aProjectInfoController.do?upload', "aProjectInfoList");
}

//导出
function ExportXls() {
	JeecgExcelExport("aProjectInfoController.do?exportXls","aProjectInfoList");
}

//模板下载
function ExportXlsByT() {
	JeecgExcelExport("aProjectInfoController.do?exportXlsByT","aProjectInfoList");
}

 </script>