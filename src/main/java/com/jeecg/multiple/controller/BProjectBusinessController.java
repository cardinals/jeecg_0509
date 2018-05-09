package com.jeecg.multiple.controller;
import com.jeecg.business.entity.AProjectInfoEntity;
import com.jeecg.business.service.AProjectInfoServiceI;
import com.jeecg.childbusiness.entity.BChildBusinessEntity;
import com.jeecg.childbusiness.service.BChildBusinessServiceI;
import com.jeecg.materialsupload.entity.AMaterialsUploadEntity;
import com.jeecg.materialsupload.service.AMaterialsUploadServiceI;
import com.jeecg.multiple.entity.BProjectBusinessEntity;
import com.jeecg.multiple.service.BProjectBusinessServiceI;

import java.io.File;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jeecgframework.core.util.*;
import org.jeecgframework.web.system.pojo.base.TSUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.exception.BusinessException;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.common.TreeChildCount;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.pojo.base.TSDepart;
import org.jeecgframework.web.system.service.SystemService;

import java.io.OutputStream;

import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.TemplateExportParams;
import org.jeecgframework.poi.excel.entity.vo.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.vo.TemplateExcelConstants;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.jeecgframework.core.beanvalidator.BeanValidators;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jeecgframework.jwt.util.GsonUtil;
import org.jeecgframework.jwt.util.ResponseMessage;
import org.jeecgframework.jwt.util.Result;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @Title: Controller
 * @Description: 并联业务信息
 * @author onlineGenerator
 * @date 2018-05-06 10:43:56
 * @version V1.0
 *
 */
@Api(value="BProjectBusiness",description="并联业务信息",tags="bProjectBusinessController")
@Controller
@RequestMapping("/bProjectBusinessController")
public class BProjectBusinessController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(BProjectBusinessController.class);

	@Autowired
	private BChildBusinessServiceI bChildBusinessService;
	@Autowired
	private AProjectInfoServiceI aProjectInfoService;
	@Autowired
	private BProjectBusinessServiceI bProjectBusinessService;
	@Autowired
	private SystemService systemService;
	@Autowired
	private AMaterialsUploadServiceI aMaterialsUploadService;
	@Autowired
	private Validator validator;



	/**
	 * 并联业务信息新增页面跳转
	 *
	 * @return
	 */
	@RequestMapping(params = "accept")
	public ModelAndView accept(AProjectInfoEntity aProjectInfo,BProjectBusinessEntity bProjectBusiness, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(aProjectInfo.getId())) {
			aProjectInfo = aProjectInfoService.getEntity(AProjectInfoEntity.class, aProjectInfo.getId());
			req.setAttribute("aProjectInfoPage", aProjectInfo);
		}
		if (StringUtil.isNotEmpty(bProjectBusiness.getId())) {
			bProjectBusiness = bProjectBusinessService.getEntity(BProjectBusinessEntity.class, bProjectBusiness.getId());
			req.setAttribute("bProjectBusinessPage", bProjectBusiness);
			req.setAttribute("businessId", DateUtils.date2Str(new SimpleDateFormat("yyyyMMddHHmmss")));
		}
		return new ModelAndView("com/jeecg/multiple/bProjectBusiness-add");
	}

	/**
	 * 并联业务跳转至材料上传页面
	 *
	 * @return
	 */
	@RequestMapping(params = "materialList")
	public ModelAndView materialList(BProjectBusinessEntity bProjectBusiness, HttpServletRequest req,DataGrid dataGrid) {
		if (StringUtil.isNotEmpty(bProjectBusiness.getId())) {
			bProjectBusiness = bProjectBusinessService.getEntity(BProjectBusinessEntity.class, bProjectBusiness.getId());
			req.setAttribute("bProjectBusinessPage", bProjectBusiness);
		}
		TSUser user = ResourceUtil.getSessionUser();
		String sql ="";
		if (ResourceUtil.getConfigByName("accept_deptid").equals(user.getCurrentDepart().getId())){
			sql = "select f.id,substr(f.materials_name,37) as file_name, f.materials_path,e.business_id,a.project_id, a.project_name, b.phases_id, b.phases_name, c.items_id || c.items_child_id as items_id, " +
					"c.items_child_name, d.materials_id, d.materials_name, c.dept_id, c.dept_name from A_PROJECT_INFO a, " +
					"A_PHASES_INFO b, A_ITEMS_INFO c, A_materials_INFO d, b_project_business e, a_materials_upload f where " +
					"a.project_id = b.project_id and b.phases_id = c.phases_id and c.items_id || c.items_child_id = d.items_id " +
					" and e.project_id = a.project_id"+
					" and f.project_id = a.project_id"+
					" and f.phases_id = b.phases_id"+
					" and f.business_id = e.business_id"+
					" and f.items_id = c.items_id || c.items_child_id"+
					" and f.materials_id = d.materials_id"+
					" and e.business_id = '"+bProjectBusiness.getBusinessId()+"'";
		}else{
			sql = "select f.id,substr(f.materials_name,37) as file_name, f.materials_path,e.business_id,a.project_id, a.project_name, b.phases_id, b.phases_name, c.items_id || c.items_child_id as items_id, " +
					"c.items_child_name, d.materials_id, d.materials_name, c.dept_id, c.dept_name from A_PROJECT_INFO a, " +
					"A_PHASES_INFO b, A_ITEMS_INFO c, A_materials_INFO d, b_project_business e, a_materials_upload f where " +
					"a.project_id = b.project_id and b.phases_id = c.phases_id and c.items_id || c.items_child_id = d.items_id " +
					" and e.project_id = a.project_id"+
					" and f.project_id = a.project_id"+
					" and f.phases_id = b.phases_id"+
					" and f.business_id = e.business_id"+
					" and f.items_id = c.items_id || c.items_child_id"+
					" and f.materials_id = d.materials_id"+
					" and e.business_id = '"+bProjectBusiness.getBusinessId()+"'" +
				" and c.dept_id = '"+user.getCurrentDepart().getId()+"'" ;
		}

		List<Map<String, Object>> materialList =  systemService.findForJdbc(sql);
		req.setAttribute("materialList", materialList);
		return new ModelAndView("com/jeecg/multiple/bBusinessMaterailList");
	}


	/**
	 * 并联业务跳转至证照上传页面
	 *
	 * @return
	 */
	@RequestMapping(params = "certificateList")
	public ModelAndView certificateList(BProjectBusinessEntity bProjectBusiness, HttpServletRequest req,DataGrid dataGrid) {
		if (StringUtil.isNotEmpty(bProjectBusiness.getId())) {
			bProjectBusiness = bProjectBusinessService.getEntity(BProjectBusinessEntity.class, bProjectBusiness.getId());
			req.setAttribute("bProjectBusinessPage", bProjectBusiness);
		}
		TSUser user = ResourceUtil.getSessionUser();
		String sql = "select * from B_CHILD_BUSINESS t where t.business_id = '"+bProjectBusiness.getBusinessId()+"'";
		List<Map<String, Object>> certificateList =  systemService.findForJdbc(sql);
		req.setAttribute("certificateList", certificateList);
		return new ModelAndView("com/jeecg/multiple/bBusinessCertificateList");
	}

//~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * 并联业务信息列表 页面跳转
	 *
	 * @return
	 */
	@RequestMapping(params = "list")
	public ModelAndView list(HttpServletRequest request) {
		return new ModelAndView("com/jeecg/multiple/bProjectBusinessList");
	}

	/**
	 * easyui AJAX请求数据
	 *
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */

	@RequestMapping(params = "datagrid")
	public void datagrid(BProjectBusinessEntity bProjectBusiness,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		TSUser user = ResourceUtil.getSessionUser();
		CriteriaQuery cq = new CriteriaQuery(BProjectBusinessEntity.class, dataGrid);
		//查询条件组装器
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, bProjectBusiness, request.getParameterMap());
		try{
			//预受理部门id 为 ResourceUtil.getConfigByName("accept_deptid")
			if (ResourceUtil.getConfigByName("accept_deptid").equals(user.getCurrentDepart().getId())){
			}else{
				String sql = "select business_id from B_CHILD_BUSINESS t where t.dept_id ='"+user.getCurrentDepart().getId()+"'";
//				String sql = "select business_id from B_CHILD_BUSINESS t where t.dept_id ='8a8ab0b246dc81120146dc8180a20016'";
				List<Map<String, Object>> list =  systemService.findForJdbc(sql);
				List<String> arrayList = new ArrayList<String>();
				if(list!=null && list.size()>0){
					for(int i=0;i<list.size();i++){
						Map<String, Object> obj =  list.get(i);
						arrayList.add(String.valueOf(obj.get("business_id")));
					}
				}
				Object[] keyvalue = arrayList.toArray();
				cq.in("businessId", keyvalue);
			}
//
		//自定义追加查询条件
		}catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
		cq.add();
		this.bProjectBusinessService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除并联业务信息
	 *
	 * @return
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson doDel(BProjectBusinessEntity bProjectBusiness, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		bProjectBusiness = systemService.getEntity(BProjectBusinessEntity.class, bProjectBusiness.getId());
		message = "并联业务信息删除成功";
		try{
			bProjectBusinessService.delete(bProjectBusiness);
			systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "并联业务信息删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

	/**
	 * 批量删除并联业务信息
	 *
	 * @return
	 */
	 @RequestMapping(params = "doBatchDel")
	@ResponseBody
	public AjaxJson doBatchDel(String ids,HttpServletRequest request){
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "并联业务信息删除成功";
		try{
			for(String id:ids.split(",")){
				BProjectBusinessEntity bProjectBusiness = systemService.getEntity(BProjectBusinessEntity.class,
				id
				);
				bProjectBusinessService.delete(bProjectBusiness);
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "并联业务信息删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}


	/**
	 * 添加并联业务信息
	 *
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doAdd")
	@ResponseBody
	public AjaxJson doAdd(BProjectBusinessEntity bProjectBusiness, HttpServletRequest request) {
		TSUser user = ResourceUtil.getSessionUser();
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "并联业务信息添加成功";
		try{
			bProjectBusiness.setCurrentPhases(bProjectBusiness.getProjectId() + "-001");
			bProjectBusiness.setProjectStatus("1");
			bProjectBusinessService.save(bProjectBusiness);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
			//1.插入子项业务信息
			String itemsSql ="select func_getnewid(20) as child_business,a.project_id," +
					"       a.project_name," +
					"       b.phases_id," +
					"       b.phases_name," +
					"       c.items_id || c.items_child_id as items_id," +
					"       c.items_child_name," +
					"       c.dept_id," +
					"       c.dept_name" +
					"  from A_PROJECT_INFO   a," +
					"       A_PHASES_INFO    b," +
					"       A_ITEMS_INFO     c " +
					" where a.project_id = b.project_id" +
					"   and b.phases_id = c.phases_id" +
					"   and a.project_id = '"+bProjectBusiness.getProjectId()+"' order by b.phases_id,c.items_id";
			List<Map<String, Object>> itemsList =  systemService.findForJdbc(itemsSql);
			if(itemsList!=null && itemsList.size()>0){
				for(int i=0;i<itemsList.size();i++){
					BChildBusinessEntity bChildBusiness = new BChildBusinessEntity();
					Map<String, Object> obj =  itemsList.get(i);
					bChildBusiness.setBusinessId(bProjectBusiness.getBusinessId());
					bChildBusiness.setChildBusinessId(String.valueOf(obj.get("child_business")));
					bChildBusiness.setProjectId(bProjectBusiness.getProjectId());
					bChildBusiness.setPhasesId(String.valueOf(obj.get("phases_id")));
					bChildBusiness.setItemsId(String.valueOf(obj.get("items_id")));
					bChildBusiness.setItemsName(String.valueOf(obj.get("items_child_name")));
					bChildBusiness.setDeptId(String.valueOf(obj.get("dept_id")));
					bChildBusiness.setDeptName(String.valueOf(obj.get("dept_name")));
					bChildBusiness.setRealityProjectName(bProjectBusiness.getRealityProjectName());
					bChildBusiness.setCreateTime(new Date());
					bChildBusinessService.save(bChildBusiness);
				}
			}

			//2.插入该项目的所有材料信息
			String sql = "select a.project_id, a.project_name, b.phases_id, b.phases_name, " +
					"c.items_id || c.items_child_id as items_id, c.items_child_name, d.materials_id, d.materials_name, " +
					"c.dept_id, c.dept_name from A_PROJECT_INFO a, A_PHASES_INFO b, A_ITEMS_INFO c, " +
					"A_materials_INFO d where a.project_id = b.project_id and b.phases_id = c.phases_id and " +
					"c.items_id || c.items_child_id = d.items_id and a.project_id = '"+bProjectBusiness.getProjectId()+"'" +
					" and substr(b.phases_id,-3) ='001'";
			List<Map<String, Object>> resultList =  systemService.findForJdbc(sql);
			if(resultList!=null && resultList.size()>0){
				for(int i=0;i<resultList.size();i++){
					AMaterialsUploadEntity aMaterialsUpload = new AMaterialsUploadEntity();
					Map<String, Object> obj =  resultList.get(i);
					aMaterialsUpload.setBusinessId(bProjectBusiness.getBusinessId());
					aMaterialsUpload.setProjectId(bProjectBusiness.getProjectId());
					aMaterialsUpload.setPhasesId(String.valueOf(obj.get("phases_id")));
					aMaterialsUpload.setItemsId(String.valueOf(obj.get("items_id")));
					aMaterialsUpload.setMaterialsId(String.valueOf(obj.get("materials_id")));
					aMaterialsUpload.setStatus("1");
					aMaterialsUpload.setCreateTime(new Date());
					aMaterialsUploadService.save(aMaterialsUpload);

				}
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "并联业务信息添加失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

	/**
	 * 更新并联业务信息
	 *
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doUpdate")
	@ResponseBody
	public AjaxJson doUpdate(BProjectBusinessEntity bProjectBusiness, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "并联业务信息更新成功";
		BProjectBusinessEntity t = bProjectBusinessService.get(BProjectBusinessEntity.class, bProjectBusiness.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(bProjectBusiness, t);
			bProjectBusinessService.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "并联业务信息更新失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}


	/**
	 * 并联业务信息新增页面跳转
	 *
	 * @return
	 */
	@RequestMapping(params = "goAdd")
	public ModelAndView goAdd(BProjectBusinessEntity bProjectBusiness, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(bProjectBusiness.getId())) {
			bProjectBusiness = bProjectBusinessService.getEntity(BProjectBusinessEntity.class, bProjectBusiness.getId());
			req.setAttribute("bProjectBusinessPage", bProjectBusiness);
		}
		return new ModelAndView("com/jeecg/multiple/bProjectBusiness-add");
	}
	/**
	 * 并联业务信息编辑页面跳转
	 *
	 * @return
	 */
	@RequestMapping(params = "goUpdate")
	public ModelAndView goUpdate(BProjectBusinessEntity bProjectBusiness, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(bProjectBusiness.getId())) {
			bProjectBusiness = bProjectBusinessService.getEntity(BProjectBusinessEntity.class, bProjectBusiness.getId());
			req.setAttribute("bProjectBusinessPage", bProjectBusiness);
		}
		return new ModelAndView("com/jeecg/multiple/bProjectBusiness-update");
	}

	/**
	 * 导入功能跳转
	 *
	 * @return
	 */
	@RequestMapping(params = "upload")
	public ModelAndView upload(HttpServletRequest req) {
		req.setAttribute("controller_name","bProjectBusinessController");
		return new ModelAndView("common/upload/pub_excel_upload");
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXls")
	public String exportXls(BProjectBusinessEntity bProjectBusiness,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid,ModelMap modelMap) {
		CriteriaQuery cq = new CriteriaQuery(BProjectBusinessEntity.class, dataGrid);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, bProjectBusiness, request.getParameterMap());
		List<BProjectBusinessEntity> bProjectBusinesss = this.bProjectBusinessService.getListByCriteriaQuery(cq,false);
		modelMap.put(NormalExcelConstants.FILE_NAME,"并联业务信息");
		modelMap.put(NormalExcelConstants.CLASS,BProjectBusinessEntity.class);
		modelMap.put(NormalExcelConstants.PARAMS,new ExportParams("并联业务信息列表", "导出人:"+ResourceUtil.getSessionUser().getRealName(),
			"导出信息"));
		modelMap.put(NormalExcelConstants.DATA_LIST,bProjectBusinesss);
		return NormalExcelConstants.JEECG_EXCEL_VIEW;
	}
	/**
	 * 导出excel 使模板
	 *
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXlsByT")
	public String exportXlsByT(BProjectBusinessEntity bProjectBusiness,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid,ModelMap modelMap) {
    	modelMap.put(NormalExcelConstants.FILE_NAME,"并联业务信息");
    	modelMap.put(NormalExcelConstants.CLASS,BProjectBusinessEntity.class);
    	modelMap.put(NormalExcelConstants.PARAMS,new ExportParams("并联业务信息列表", "导出人:"+ResourceUtil.getSessionUser().getRealName(),
    	"导出信息"));
    	modelMap.put(NormalExcelConstants.DATA_LIST,new ArrayList());
    	return NormalExcelConstants.JEECG_EXCEL_VIEW;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(params = "importExcel", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson importExcel(HttpServletRequest request, HttpServletResponse response) {
		AjaxJson j = new AjaxJson();
		
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile file = entity.getValue();// 获取上传文件对象
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				List<BProjectBusinessEntity> listBProjectBusinessEntitys = ExcelImportUtil.importExcel(file.getInputStream(),BProjectBusinessEntity.class,params);
				for (BProjectBusinessEntity bProjectBusiness : listBProjectBusinessEntitys) {
					bProjectBusinessService.save(bProjectBusiness);
				}
				j.setMsg("文件导入成功！");
			} catch (Exception e) {
				j.setMsg("文件导入失败！");
				logger.error(ExceptionUtil.getExceptionMessage(e));
			}finally{
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return j;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value="并联业务信息列表信息",produces="application/json",httpMethod="GET")
	public ResponseMessage<List<BProjectBusinessEntity>> list() {
		List<BProjectBusinessEntity> listBProjectBusinesss=bProjectBusinessService.getList(BProjectBusinessEntity.class);
		return Result.success(listBProjectBusinesss);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value="根据ID获取并联业务信息信息",notes="根据ID获取并联业务信息信息",httpMethod="GET",produces="application/json")
	public ResponseMessage<?> get(@ApiParam(required=true,name="id",value="ID")@PathVariable("id") String id) {
		BProjectBusinessEntity task = bProjectBusinessService.get(BProjectBusinessEntity.class, id);
		if (task == null) {
			return Result.error("根据ID获取并联业务信息信息为空");
		}
		return Result.success(task);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="创建并联业务信息")
	public ResponseMessage<?> create(@ApiParam(name="并联业务信息对象")@RequestBody BProjectBusinessEntity bProjectBusiness, UriComponentsBuilder uriBuilder) {
		//调用JSR303 Bean Validator进行校验，如果出错返回含400错误码及json格式的错误信息.
		Set<ConstraintViolation<BProjectBusinessEntity>> failures = validator.validate(bProjectBusiness);
		if (!failures.isEmpty()) {
			return Result.error(JSONArray.toJSONString(BeanValidators.extractPropertyAndMessage(failures)));
		}

		//保存
		try{
			bProjectBusinessService.save(bProjectBusiness);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("并联业务信息信息保存失败");
		}
		return Result.success(bProjectBusiness);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="更新并联业务信息",notes="更新并联业务信息")
	public ResponseMessage<?> update(@ApiParam(name="并联业务信息对象")@RequestBody BProjectBusinessEntity bProjectBusiness) {
		//调用JSR303 Bean Validator进行校验，如果出错返回含400错误码及json格式的错误信息.
		Set<ConstraintViolation<BProjectBusinessEntity>> failures = validator.validate(bProjectBusiness);
		if (!failures.isEmpty()) {
			return Result.error(JSONArray.toJSONString(BeanValidators.extractPropertyAndMessage(failures)));
		}

		//保存
		try{
			bProjectBusinessService.saveOrUpdate(bProjectBusiness);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("更新并联业务信息信息失败");
		}

		//按Restful约定，返回204状态码, 无内容. 也可以返回200状态码.
		return Result.success("更新并联业务信息信息成功");
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value="删除并联业务信息")
	public ResponseMessage<?> delete(@ApiParam(name="id",value="ID",required=true)@PathVariable("id") String id) {
		logger.info("delete[{}]" + id);
		// 验证
		if (StringUtils.isEmpty(id)) {
			return Result.error("ID不能为空");
		}
		try {
			bProjectBusinessService.deleteEntityById(BProjectBusinessEntity.class, id);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("并联业务信息删除失败");
		}

		return Result.success();
	}
}
