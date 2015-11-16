var loadUpdate = function() {
	var updates = getAvailableUpdatesCallback();
	var updatesMap = $.parseJSON(updates);
	$("#updateTab").data("updatesMap", updatesMap);
	//var count = 0;
	$.each(updatesMap, function(key, value) {
		var item = createUpdateItem(key, value);
		$("#updateList").append(item);
		//$("#updateList").append("<hr>");
		//count++;
	});
	//$("#updateSummary").text(count + " updates are availbale.");
}

var createUpdateItem = function(id, feature) {
	var item = $.parseHTML("<li class='media'></li>");
	$(item).attr("id", id);
	$(item).data("feature", feature);

	var header = $.parseHTML("<a href='#' class='pull-left'></a>");

	var icon = $.parseHTML("<img class='img-circle'>");
	$(icon).attr("src", feature.iconURL);

	$(header).append(icon);
	$(item).append(header);

	var body = $.parseHTML("<div class='media-body'></div>");
	var version = $
			.parseHTML("<span class='text-muted pull-right'><small class='text-muted'>"
					+ feature.version + "</small></span>");
	$(body).append(version);

	var label = $.parseHTML("<strong class='text-success'>" + feature.label
			+ "</strong>");
	$(body).append(label);

	var installBtn = $
			.parseHTML("<span class='glyphicon glyphicon-save insall-icon'></span>");
	$(body).append(installBtn);

	var description = $.parseHTML("<p></p>");
	$(description).innerHTML(feature.description);

	$(item).append(body);

	return item;
}

var getAvailableUpdatesCallback = function(){
	return {  
		   'org.wso2.developerstudio.capp.feature.feature.group':{  
			      'installable':false,
			      'selected':false,
			      'id':'org.wso2.developerstudio.capp.feature.feature.group',
			      'label':'Carbon Application Tools',
			      'version':'4.0.0.M1',
			      'provider':'%providerName',
			      'descriptionURL':'http://wso2.com',
			      'description':'This feature will install plugins related WSO2 Carbon Application Support.',
			      'iconURL':'/tmp/DevSUpdaterTmp/org.wso2.developerstudio.capp.feature.feature.jar/4.0.0.M1/extracted/icon.png'
			   },
			   'org.wso2.developerstudio.registry.feature.feature.group':{  
			      'installable':false,
			      'selected':false,
			      'id':'org.wso2.developerstudio.registry.feature.feature.group',
			      'label':'Registry Tools',
			      'version':'4.0.0.M1',
			      'provider':'%providerName',
			      'descriptionURL':'http://wso2.com',
			      'description':'This feature will install plugins related to Carbon Registry Tools.',
			      'iconURL':'/tmp/DevSUpdaterTmp/org.wso2.developerstudio.registry.feature.feature.jar/4.0.0.M1/extracted/icon.png'
			   },
			   'org.wso2.developerstudio.kernel.dependencies.feature.feature.group':{  
			      'installable':false,
			      'selected':false,
			      'id':'org.wso2.developerstudio.kernel.dependencies.feature.feature.group',
			      'label':'Kernel Dependencies',
			      'version':'4.0.0.M1',
			      'provider':'%providerName',
			      'descriptionURL':'http://wso2.com',
			      'description':'Do not install this feature manually. WSO2 Developer Studio Kernel Dependencies Feature.',
			      'iconURL':'/tmp/DevSUpdaterTmp/org.wso2.developerstudio.kernel.dependencies.feature.feature.jar/4.0.0.M1/extracted/icon.png'
			   },
			   'org.wso2.developerstudio.usermgt.core.feature.feature.group':{  
			      'installable':false,
			      'selected':false,
			      'id':'org.wso2.developerstudio.usermgt.core.feature.feature.group',
			      'label':'User Management Core',
			      'version':'4.0.0.M1',
			      'provider':'%providerName',
			      'descriptionURL':'http://wso2.com',
			      'description':'This feature will install core plugins for User Management.',
			      'iconURL':'/tmp/DevSUpdaterTmp/org.wso2.developerstudio.usermgt.core.feature.feature.jar/4.0.0.M1/extracted/icon.png'
			   },
			   'org.wso2.developerstudio.kernel.pluginsamples.feature.feature.group':{  
			      'installable':false,
			      'selected':false,
			      'id':'org.wso2.developerstudio.kernel.pluginsamples.feature.feature.group',
			      'label':'Samples',
			      'version':'4.0.0.M1',
			      'provider':'%providerName',
			      'descriptionURL':'http://wso2.com',
			      'description':'This feature contains the sample plugins for developer studio kernel.',
			      'iconURL':'/tmp/DevSUpdaterTmp/org.wso2.developerstudio.kernel.pluginsamples.feature.feature.jar/4.0.0.M1/extracted/icon.png'
			   },
			   'org.wso2.developerstudio.kernel.webeditor.tools.feature.feature.group':{  
			      'installable':false,
			      'selected':false,
			      'id':'org.wso2.developerstudio.kernel.webeditor.tools.feature.feature.group',
			      'label':'Web Based Editor Framework',
			      'version':'4.0.0.M1',
			      'provider':'%providerName',
			      'descriptionURL':'http://wso2.com',
			      'description':'This feature will install plugins related to Web Based Editor Framework.',
			      'iconURL':'/tmp/DevSUpdaterTmp/org.wso2.developerstudio.kernel.webeditor.tools.feature.feature.jar/4.0.0.M1/extracted/icon.png'
			   },
			   'org.wso2.developerstudio.kernel.core.feature.feature.group':{  
			      'installable':false,
			      'selected':false,
			      'id':'org.wso2.developerstudio.kernel.core.feature.feature.group',
			      'label':'Core Plugins',
			      'version':'4.0.0.M1',
			      'provider':'%providerName',
			      'descriptionURL':'http://wso2.com',
			      'description':'This feature will install mandatory kernel plugins.',
			      'iconURL':'/tmp/DevSUpdaterTmp/org.wso2.developerstudio.kernel.core.feature.feature.jar/4.0.0.M1/extracted/icon.png'
			   }
			};
}