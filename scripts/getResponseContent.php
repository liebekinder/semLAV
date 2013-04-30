<?php
	//indique que le type renvoyé sera du texte
	//mauvaise méthode mais plus simple à utiliser :/
	//devrait créer des noeuds, fils et parent. Cependant, valide w3c
	header("Content-type: text/html ; charset=utf-8");
	
	//anti cache http 1.1
	header("Cache-Control: no-cache, private");
	
	//anticache pour http 1.0
	header("Pragma: no-cache");

	if(isset($_REQUEST['query'])){
		if($_REQUEST['query']){
			include_once 'functions.php';
			echo getResponseContent($_REQUEST['query']);
		}
	else{
		echo "Failed to execute Ajax instruction. GET request is empty";
	}
	}
	else{
		echo "Failed to execute Ajax instruction. GET request is null";
	}
?>