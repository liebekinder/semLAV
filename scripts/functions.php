<?php

function getViewContent($queryName){
	$Fichier="";
	if (!$fp = fopen("../code/expfiles/berlinData/DATASET/views/viewsSparql/$queryName.sparql","r")) {
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
	return "<textarea readonly style=\"width:440px; height:250px; resize:none;\">".$Fichier."</textarea>";
}

function getQueryContent($queryName){
	$Fichier="";
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

function getQueryAnswers($queryNumber){
	//execute semlavBerlindata
// 	echo getcwd();
	putenv("GUNPATH = /var/www/semLAV/code/");
	chdir("/var/www/semLAV/code/queryExecutor/src");
	
	$cmd = "sh /var/www/semLAV/code/queryExecutor/src/runBerlinSemLAV.sh $queryNumber $queryNumber";
// 	echo $cmd;
	$output = shell_exec("sh /var/www/semLAV/code/queryExecutor/src/runBerlinSemLAV.sh $queryNumber");
// 	$output = shell_exec("ls");
	
	if($output == NULL ) return "le retour de shell_exec est nul";
	else if($output == "") return "le retour de shell_exec est vide";
	else if($output == "\n") return "le retour de shell_exec est retour ligne";
	else return analyseQueryAnswers($queryNumber, $output);
}

function analyseQueryAnswers($queryNumber, $output){
	//on suppose l'exécution terminée. Lecture des fichiers.
	$graphsize = "NA";
	$graphtime = "NA";
	$nbanswers = "NA";
	$firstanswers = "NA";
	
	
// 	sleep(20);
	//throughput
	$lignePre="";
// 	echo getcwd();
	$fic = "/var/www/semLAV/code/expfiles/berlinOutput/DATASET/views/outputRelViewsquery$queryNumber/NOTHING/throughput";
// 	echo $fic;
	if (!$fp = fopen($fic,"r")) {
		echo "Echec de l'ouverture du fichier";
		exit;
	}
	else {
		while(!feof($fp)) {
			// On récupère une ligne
			$Ligne = fgets($fp,255);
// 			echo $Ligne;
			// On affiche la ligne
			//echo $Ligne;
			if(strlen($Ligne)>10){
				$lignePre = $Ligne;
			}
	
		}
		fclose($fp); // On ferme le fichier
		$tab = explode("\t",$lignePre);
// 		var_dump($tab);
		if(strlen($tab[0]) <=2){
			$graphsize = $tab[6];
			$graphtime = $tab[5];
		}
	}
	$lignePre="";
// 	echo getcwd();
	$fic = "file:///var/www/semLAV/code/expfiles/berlinOutput/DATASET/views/outputRelViewsquery$queryNumber/NOTHING/answersInfo";
// 	echo $fic;
	if (!$fp = fopen($fic,"r")) {
		echo "Echec de l'ouverture du fichier";
		exit;
	}
	else {
		while(!feof($fp)) {
			// On récupère une ligne
			$Ligne = fgets($fp,255);
// 			echo $Ligne;
			// On affiche la ligne
			//echo $Ligne;
			if(strlen($Ligne)>10){
				$lignePre = $Ligne;
			}
	
		}
		fclose($fp); // On ferme le fichier
		$tab = explode("\t",$lignePre);
// 		var_dump($tab);
		if(strlen($tab[0]) <=2){
			$nbanswers = $tab[5];
		}
	}
	

	$views = explode("\n", $output);
	//var_dump($views);
	
	
	return displayQueryanswer($queryNumber, $graphsize, $graphtime, $nbanswers, $firstanswers, $views);
}

function displayQueryanswer($queryNumber, $graphsize, $graphtime, $nbanswers, $firstanswers, $views){
	$retour = "<table id='laTable'>";
	$retour .= "<tr><td colspan='2' style='text-align:center;'>";
	$retour .= "<i><u>Query$queryNumber</u></i>";
	$retour .= "</td></tr>";
	$retour .= "<tr><td colspan='2'><br/></td></tr>";
	$retour .= "<tr><td colspan='2'><br/></td></tr>";
	$retour .= "<tr><td>Nombre de réponse:</td><td>$nbanswers</td></tr>";
	$retour .= "<tr><td>temps total d'exécution:</td><td>$graphtime ms</td></tr>";
	$retour .= "<tr><td>taille du graph:</td><td>$graphsize</td></tr>";
	$retour .= "<tr><td colspan='2'><br/></td></tr>";
	$retour .= "<tr><td colspan='2'><br/></td></tr>";
	$retour .= "<tr><td colspan='2' style='text-align:center;'><i><u>Relevant Views</u></i></td></tr>";
	foreach($views as $vue){		
		$retour .= "<tr><td colspan='2' style='text-align:center;'><a href='javascript:displayView(\"$vue\");'>$vue</a></td></tr>";
	}	
	//$retour .= "<tr><td>time to first answer:</td><td>$firstanswers</td></tr>";
	
	$retour .= "</table>";
	return $retour;
}
?>