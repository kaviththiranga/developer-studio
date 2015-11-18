
var countSelectedUpdates = 0;


$(document).ready(function () {
	$(document).on("click", ".update-icon", function(e) {
		var item = $(this).closest(".media");
		var feature = $(item).data("feature");
		var activeClass = "selected";

		// feature is already selected - this is to unselect
		if (feature.selected) {
			feature.selected = false;
			$(this).removeClass(activeClass);
			countSelectedUpdates--;
		} else {
			feature.selected = true;
			$(this).addClass(activeClass);
			countSelectedUpdates++;
		}
		var updateListTxt = "";
		if (countSelectedUpdates === 0) {
			updateListTxt = " No features will be updated.";
		} else if (countSelectedUpdates === 1) {
			updateListTxt = " 1 feature will be updated.";
		} else {
			updateListTxt = countSelectedUpdates
					+ " features will be updated.";
		}
		$("#updateSelectionSummary").text(updateListTxt);
	});
});

var installSelectedUpdates = function(){
	//var updates = $("#updateTab").data("updatesMap");
	var selectedUpdates = [];
	
	$('#updateList').children('.media').each(function () {
		var feature =  $(this).data("feature");
		if(feature.selected){
			selectedUpdates.push(feature);
		}
	});
	setSelectedUpdatesCallback(JSON.stringify(selectedUpdates));
}


var addToolTips = function(){
    $('[data-toggle="tooltip"]').tooltip();   
};


var initUpdatesList = function() {
	try {
		var updatesString = getAvailableUpdatesCallback();
		var updates = $.parseJSON(updatesString);
		$("#updateTab").data("updatesMap", updates);
		var count = 0;
		$.each(updates, function(key, value) {
			var item = createUpdateItem(key, value);
			$(item).data("feature", value);
			$("#updateList").append(item);
			// $("#updateList").append("<hr>");
			count++;
		});
		$("#updateAvailableSummary").text(count + " updates are availbale.");
		$("#updateSelectionSummary").text(
				countSelectedUpdates + " updates will be installed.");
		addToolTips();
	} catch (err) {
		alert('Error while loading list of updates. Message: ' + err.message);
	}
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
			.parseHTML("<span class='text-muted pull-right'><small class='text-muted'>version: "
					+ feature.version + "</small></span>");
	$(body).append(version);

	var label = $.parseHTML("<strong class='text-success'>" + feature.label
			+ "</strong>");
	$(body).append(label);

	var installBtn = $
			.parseHTML("<span class='glyphicon glyphicon-save update-icon' data-toggle='tooltip' title='install "
					+ feature.label+ " " + feature.version + "'></span>");
	$(body).append(installBtn);

	var description = $.parseHTML("<p></p>");
	$(description).text(feature.description);
	$(body).append(description);

	$(item).append(body);

	return item;
}


var getAvailableUpdatesCallbackTest = function(){
  var map = '{"org.wso2.developerstudio.carbon.server.feature.feature.group":{"currentVersion":"4.0.0.201511171257","installable":false,"selected":false,"id":"org.wso2.developerstudio.carbon.server.feature.feature.group","label":"Carbon Server Tools","version":"4.0.0.201511171304","provider":"%providerName","descriptionURL":"http://wso2.com","description":"This feature will install plugins related to Carbon Server Tools.","iconURL":"/tmp/DevSUpdaterTmp/org.wso2.developerstudio.carbon.server.feature.feature.jar/4.0.0.201511171304/extracted/icon.png"},"org.wso2.developerstudio.capp.feature.feature.group":{"currentVersion":"4.0.0.201511171257","whatIsNew":"This property tells users about new things in this release/update. You can add a nice description about the new things in here.","bugFixes":"This property tells users about fixes done in this release/patch.You can inform users about the fixes you have done in this patch.","installable":false,"selected":false,"id":"org.wso2.developerstudio.capp.feature.feature.group","label":"Carbon Application Tools","version":"4.0.0.201511171304","provider":"%providerName","descriptionURL":"http://wso2.com","description":"This feature will install plugins related WSO2 Carbon Application Support.","iconURL":"/tmp/DevSUpdaterTmp/org.wso2.developerstudio.capp.feature.feature.jar/4.0.0.201511171304/extracted/icon.png"},"org.wso2.developerstudio.registry.feature.feature.group":{"currentVersion":"4.0.0.201511171257","installable":false,"selected":false,"id":"org.wso2.developerstudio.registry.feature.feature.group","label":"Registry Tools","version":"4.0.0.201511171304","provider":"%providerName","descriptionURL":"http://wso2.com","description":"This feature will install plugins related to Carbon Registry Tools.","iconURL":"/tmp/DevSUpdaterTmp/org.wso2.developerstudio.registry.feature.feature.jar/4.0.0.201511171304/extracted/icon.png"}}';
	return map;
}