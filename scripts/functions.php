<?php

function getViewContent($queryName){
	$Fichier="";
	if (!$fp = fopen("../code/expfiles/berlinData/DATASET/views/viewsSparql/$queryName.sparql","r")) {
		echo "Failed to open file".$fic;	
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
		echo "Failed to open file".$fic;
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
	
// 	$cmd = "sh /var/www/semLAV/code/queryExecutor/src/runBerlinSemLAV.sh $queryNumber $queryNumber";
// 	echo $cmd;
	$output = shell_exec("sh /var/www/semLAV/code/queryExecutor/src/runBerlinSemLAV.sh $queryNumber");
// 	$output = shell_exec("ls");
	
	if($output == NULL ) return "shell_exec error: response is null";
	else if($output == "") return "shell_exec error: response is empty";
	else if($output == "\n") return "shell_exec error: response is 'return'";
	else return analyseQueryAnswers($queryNumber, $output);
}

function analyseQueryAnswers($queryNumber, $output){
	//on suppose l'exécution terminée. Lecture des fichiers.
	$graphsize = "NA";
	$graphtime = "NA";
	$nbanswers = "NA";
	$wrapertime = "NA";
	$graphcreation = "NA";
	$exectime = "NA";
	
	$tab = explode("\t",getTimeAndSize($queryNumber));
	if(strlen($tab[0]) <=2){
		$graphsize = $tab[6];
		$graphtime = $tab[5];
		$wrapertime = $tab[2];
		$graphcreation = $tab[3];
		$exectime = $tab[4];
	}
	
	$nbanswers = getNbAnswers($queryNumber);

	$views = explode("\n", $output);
	
// 	$firstanswers = explode ("\n", getAnswers($queryNumber));
	
	
	return displayQueryanswer($queryNumber, $wrapertime, $graphcreation, $exectime, $graphsize, $graphtime, $nbanswers, $views);
}

function getTimeAndSize($queryNumber){
	sleep(20);
	//throughput
	$lignePre="";
	// 	echo getcwd();
	$fic = "/var/www/semLAV/code/expfiles/berlinOutput/DATASET/views/outputRelViewsquery$queryNumber/NOTHING/throughput";
	// 	echo $fic;
	if (!$fp = fopen($fic,"r")) {
		echo "Failed to open file".$fic;
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
		
	}
	return $lignePre;
}
function getNbAnswers($queryNumber){
	$lignePre="";
	// 	echo getcwd();
	$fic = "/var/www/semLAV/code/expfiles/berlinOutput/DATASET/views/outputRelViewsquery$queryNumber/NOTHING/answersInfo";
	// 	echo $fic;
	if (!$fp = fopen($fic,"r")) {
		echo "Failed to open number of answers file".$fic;
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
			$reponse = $tab[5];
		}
	}
	return $reponse;
	
}
function getResponseContent($queryNumber){
	$Fichier="";
	if($queryNumber == 1){
		$fic = "/var/www/semLAV/code/expfiles/berlinOutput/DATASET/views/outputRelViewsquery$queryNumber/NOTHING/solution1";
	}
	else{
		$fic = "/var/www/semLAV/code/expfiles/berlinOutput/DATASET/views/outputRelViewsquery$queryNumber/NOTHING/solution0";
	}
	if (!$fp = fopen($fic,"r")) {
		echo "Failed to open answer file".$fic;
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
// 	$firstanswers = explode ("\n", getAnswers($queryNumber));
	
	return "<div>".nl2br($Fichier)."</div>";
}

function displayQueryanswer($queryNumber, $wrapertime, $graphcreation, $exectime, $graphsize, $graphtime, $nbanswers, $views){
	$retour = "<table id='laTable'>";
	$retour .= "<tr><td colspan='2' style='text-align:center;'>";
	$retour .= "<i><u>Query$queryNumber</u></i>";
	$retour .= "</td></tr>";
	$retour .= "<tr><td colspan='2'><br/></td></tr>";
	$retour .= "<tr><td>Number of answer:</td><td>$nbanswers</td></tr>";
	$retour .= "<tr><td>Total execution time:</td><td>$graphtime ms</td></tr>";
	$retour .= "<tr><td>Graph size:</td><td>$graphsize</td></tr>";
	$retour .= "<tr><td>Wrapper Time:</td><td>$wrapertime ms</td></tr>";
	$retour .= "<tr><td>Graph Creation Time:</td><td>$graphcreation ms</td></tr>";
	$retour .= "<tr><td>Execution Time:</td><td>$exectime ms</td></tr>";
	$retour .= "<tr><td colspan='2'><br/></td></tr>";
	$retour .= "<tr><td colspan='2' style='text-align:center;'><i><u>Relevant Views</u></i></td></tr>";
	foreach($views as $vue){		
		$retour .= "<tr><td colspan='2' style='text-align:center;'><a href='javascript:displayView(\"$vue\");'";
		
		if($vue == "view1_0" || $vue == "view2_0" || $vue == "view3_0") $retour .= "title = http://data.nantes.fr/api/publication/22440002800011_CG44_TOU_04815/hotels_STBL/content";
		else if($vue == "view5_0" || $vue == "view6_0" || $vue == "view7_0" || $vue == "view8_0") $retour .= "title = https://data.nantes.fr/api/publication/22440002800011_CG44_TOU_04812/activites_tourisme_et_handicap_STBL/content";
		else if($vue == "view9_0") $retour .= "title = http://dbpedia.org/sparql";
		
		$retour .= ">".$vue;
		$retour .= "</a></td></tr>";
	}
	$retour .= "<tr><td colspan='2'><br/></td></tr>";
	$retour .= "<tr><td colspan='2' style='text-align:center;'><i><u>Answers</u></i></td></tr>";
		$retour .= "<tr><td colspan='2' style='text-align:center;'><a href='javascript:displayResponse(\"$queryNumber\");'>Click here to see the answers</a></td></tr>";
	
	$retour .= "</table>";
	return $retour;
	
	
}
?>