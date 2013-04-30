function displayView(val){
	// Moteur ajax concernant la partie gestionnaire de la page: recherche du dossier à afficher, genération du html et renvoie au client
	//contient aussi les diverses modification qu'on peut apporter aux fichiers: affichage, compression, suppression...
	/* ------ MOTEUR AJAX -----------*/
	
//	$("#laDiv").attr("title", val);
	gebi("ui-id-1").innerHTML = val;
//	gebi("laDiv").title = val;
	//creation d'un objet XHR
	objetXHR3 = null;
	objetXHR3 = creationXHR();
	
	//tromperie pour le cache
	var temps = new Date().getTime();
	var parametres = "query=" + codeVariable(val) + "&anticache=" + temps;
		
	//configuration de la requete en GET ert synchrone
	objetXHR3.open("get","scripts/getViewContent.php?" + parametres,true);
	
	//configuration de la fonction du traitement asynchrone
	objetXHR3.onreadystatechange = actualiserPage3;
	
	//chargement du loader avant que la requete soit executee et on cache le tableau
	
	//envoie de la requete
	objetXHR3.send(null);
	
	/* ------ FIN MOTEUR AJAX -------*/
}

function actualiserPage3(){
	if(objetXHR3.readyState == 4){
		if(objetXHR3.status == 200){
			
			//recuperation des resultat dans un tableau
			var nouveauResultat = objetXHR3.responseText;
			
			//modification de la page
			gebi("laDiv").innerHTML = nouveauResultat;
			
			
			//on cache le loader et on debloque le bouton et on affiche le resultat
			
			$("#laDiv").dialog("open");
		}
		else{
			gebi("laDiv").innerHTML = "erreur serveur: "+ objetXHR3.status +" - "+ objetXHR3.statusText;
			$("#laDiv").dialog("open");
			//annule la requete en cours
			objetXHR3.abort();
			objetXHR3 = null;
		}
	}
}


function creationXHR(){
	/* ------- creation de l'objet XHR en fonction du navigateur ----*/
	var resultat=null;
	try{//navigateur:mozilla, opera, ie9(?)
		resultat= new XMLHttpRequest();
	}
	catch(error){
		try{//pour ie6, ie7, ie8(?)
			resultat= new ActiveXObject("Msxml2.XMLHTTP");
		}
		catch(error){
			resultat=null;
			alert("erreur dans l'instanciation de xmlhttprequest");
		}
	}
	return resultat;
	/* --- fin creation de l'objet XHR en fonction du navigateur ----*/
}

function gebi(nom){
	//raccourci pour la fonction document.getelementById()
	return document.getElementById(nom);
}

function getQueryContent(val){
	// Moteur ajax concernant la partie gestionnaire de la page: recherche du dossier à afficher, genération du html et renvoie au client
	//contient aussi les diverses modification qu'on peut apporter aux fichiers: affichage, compression, suppression...
	/* ------ MOTEUR AJAX -----------*/
	
	//creation d'un objet XHR
	objetXHR = null;
	objetXHR = creationXHR();
	
	//tromperie pour le cache
	var temps = new Date().getTime();
	var parametres = "query=" + codeVariable(val) + "&anticache=" + temps;
		
	//configuration de la requete en GET ert synchrone
	objetXHR.open("get","scripts/getQueryContent.php?" + parametres,true);
	
	//configuration de la fonction du traitement asynchrone
	objetXHR.onreadystatechange = actualiserPage;
	
	//chargement du loader avant que la requete soit executee et on cache le tableau
	disableButton(true);
	global = val;
//	gebi("querySelector").style.visibility = "hidden";
	
	//envoie de la requete
	objetXHR.send(null);
	
	/* ------ FIN MOTEUR AJAX -------*/
}

function disableButton(disabled){
	$("#buttonExec").button("option", "disabled", false);
	$(".sendButton").button("option", "disabled", disabled);
}

function actualiserPage(){
	if(objetXHR.readyState == 4){
		if(objetXHR.status == 200){
			
			//recuperation des resultat dans un tableau
			var nouveauResultat = objetXHR.responseText;
			
			//modification de la page
			gebi("queryTextArea").innerHTML = nouveauResultat;
			
			//on cache le loader et on debloque le bouton et on affiche le resultat

			disableButton(false);

		}
		else{
			gebi("querySelector").innerHTML = "erreur serveur: "+ objetXHR.status +" - "+ objetXHR.statusText;
			disableButton(false);
			
			//annule la requete en cours
			objetXHR.abort();
			objetXHR = null;
		}
	}
}

function getQueryAnswers(){
	// Moteur ajax concernant la partie gestionnaire de la page: recherche du dossier à afficher, genération du html et renvoie au client
	//contient aussi les diverses modification qu'on peut apporter aux fichiers: affichage, compression, suppression...
	/* ------ MOTEUR AJAX -----------*/
	
	//creation d'un objet XHR
	objetXHR2 = null;
	objetXHR2 = creationXHR();
	
	//TODO
	//il faut détecter quelle requete est a effectuer. mettre la valeur dans val
	if(global != "") val = global.substring(5, 6);
	
	//tromperie pour le cache
	var temps = new Date().getTime();
	var parametres = "query=" + codeVariable(val) + "&anticache=" + temps;
		
	//configuration de la requete en GET ert synchrone
	objetXHR2.open("get","scripts/getQueryAnswers.php?" + parametres,true);
	
	//configuration de la fonction du traitement asynchrone
	objetXHR2.onreadystatechange = actualiserPage2;
	
	//chargement du loader avant que la requete soit executee et on cache le tableau
	gebi("result").style.visibility = "hidden";
	gebi("imageWait").style.visibility = "visible";
	$("#buttonExec").button("option", "disabled", true);
	
	//envoie de la requete
	objetXHR2.send(null);
	
	/* ------ FIN MOTEUR AJAX -------*/
}

function actualiserPage2(){
	if(objetXHR2.readyState == 4){
		if(objetXHR2.status == 200){
			
			//recuperation des resultat dans un tableau
			var nouveauResultat = objetXHR2.responseText;
			
			//modification de la page
			gebi("result").innerHTML = nouveauResultat;
			
			//on cache le loader et on debloque le bouton et on affiche le resultat
			gebi("result").style.visibility = "visible";
		}
		else{
			gebi("result").innerHTML = "erreur serveur: "+ objetXHR2.status +" - "+ objetXHR2.statusText;
			gebi("result").style.visibility = "visible";
			gebi("imageWait").style.visibility = "hidden";
			
			//annule la requete en cours
			objetXHR2.abort();
			objetXHR2 = null;
		}
	}

	gebi("imageWait").style.visibility = "hidden";
}


//-------------------------------------------------------------------//
//		fonctions additionnelles									//
//-----------------------------------------------------------------//

function supprimerContenu(element){
	if(element != null){
		while(element.firstChild) element.removeChild(element.firstChild);
	}
}
	
function remplacerContenu(id, texte){
	var element = gebi(id);
	if(element != null){
		supprimerContenu(element);
		var nouveauContenu = document.createTextNode(texte);
		element.appendChild(nouveauContenu);
	}
}

function codeContenu(id){
	var contenu = gebi(id).value;
	//retourne le contenu de id encode en UTF-8
	return encodeURIComponent(contenu);
}

function codeVariable(id){
	//retourne le contenu de id encode en UTF-8
	return encodeURIComponent(id);
}

function get_ex(valeur){
	temp = "";
	if(valeur.lastIndexOf(".") != -1){
		temp = valeur.substr(valeur.lastIndexOf("."), valeur.length);
	}
	return temp;
}
function addslashes(ch) {
	ch = ch.replace(/\\/g,"\\\\");
	ch = ch.replace(/\'/g,"\\'");
	ch = ch.replace(/\"/g,"\\\"");
return ch
}
