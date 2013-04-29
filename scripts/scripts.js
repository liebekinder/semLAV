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
//	gebi("querySelector").style.visibility = "hidden";
	$(".sendButton").button("option", "disabled", true);
	
	//envoie de la requete
	objetXHR.send(null);
	
	/* ------ FIN MOTEUR AJAX -------*/
}

function actualiserPage(){
	if(objetXHR.readyState == 4){
		if(objetXHR.status == 200){
			
			//recuperation des resultat dans un tableau
			var nouveauResultat = objetXHR.responseText;
			
			//modification de la page
			gebi("queryTextArea").innerHTML = nouveauResultat;
			
			//on cache le loader et on debloque le bouton et on affiche le resultat
//			gebi("querySelector").style.visibility = "visible";
		}
		else{
			gebi("querySelector").innerHTML = "erreur serveur: "+ objetXHR.status +" - "+ objetXHR.statusText;
//			gebi("querySelector").style.visibility = "visible";
			
			//annule la requete en cours
			objetXHR.abort();
			objetXHR = null;
		}
		$(".sendButton").button("option", "disabled", false);
	}
}

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