<?php

function getQueryContent($queryName){
	
	if (!$fp = fopen("../code/expfiles/berlinData/DATASET/sparqlQueries/$queryName.sparql","r")) {
		echo "Echec de l'ouverture du fichier";	
		exit;	
	}	
	else {
		while(!feof($fp)) {
			// On récupère une ligne
			$Ligne = fgets($fp,255);
	
			// On affiche la ligne
			//echo $Ligne;
	
			// On stocke l'ensemble des lignes dans une variable
			$Fichier .= $Ligne;
	
		}
		fclose($fp); // On ferme le fichier
	}
	return $Fichier;
}

function getQueryAnswers($queryName){
	//execute semlavBerlindata
// 	$output = shell_exec(". /var/www/semLAV/code/queryExecutor/src/runBerlinSemLAV.sh");
	$output = shell_exec("ls");
	
	if($output = NULL ) return "le retour de shell_exec est nul";
	else if($output == "" || $output == "\n") return "le retour de shell_exec est vide";
	else return $output;
}
?>