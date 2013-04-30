<!DOCTYPE html>

<head>
	<meta charset="UTF-8"/>
	<title>semLAV Query Handler</title>
	<link type="text/css" rel="stylesheet" href="stylesheets/stylesheet.css"/>
	<link type="text/css" rel="stylesheet" href="stylesheets/ui-lightness/jquery-ui-1.10.2.custom.css"/>
	<script type="text/javascript" src="scripts/JQuery/jquery-1.9.1.js"></script>
	<script type="text/javascript" src="scripts/JQuery/jquery-ui-1.10.2.custom.js"></script>
	<script type="text/javascript" src="scripts/scripts.js"></script>
		<script type="text/javascript">
		$(function () {
			$("#querySelector").buttonset();
			$("button").button();
		});
	</script>
</head>
<body>
 
 <noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        in order for this application to display correctly.
      </div>
    </noscript>    
	<script type="text/javascript">
	var global="";
	</script>
    <div id="principal">
    	<div id="querySelector">
    		<?php include("scripts/bouton.php");?>
    	</div>
    	<div id="queryDisplay">
    		<textarea id="queryTextArea" class="queryField" readonly></textarea>
    	</div>
    	<div id="queryResult">
    		<img id="imageWait" src="media/wait.gif" alt="wait"/>
    		<div id="calculate"><button id="buttonExec" disabled class="executeButton" onclick="getQueryAnswers();"> Evaluation de la requete!</button></div>
    		<div id="result"></div>
    	</div>
    </div>
    
</body>

<div id="laDiv"></div>

</html>
