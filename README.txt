//////////////////////////////////////////////////////////////
//					SemLav Query Handler					//
//////////////////////////////////////////////////////////////

Mise en production:
-------------------

1) copiez l'ensemble des fichiers dans l'emplacement dédié à semLAV. Ex: /var/www/semlav
2) Donnez des droits appropriés aux dossiers: il faut que le dossier code/queryExecutor/src
   soit en droit lecture/execution, que le dossier code/expfiles/berlinData(berlinOutput) soit en lecture/écriture.
   - si lors de l'exécution des erreurs apparaissent (exécution renvoie null dans la partie réponse du 
   site (le volet de droite), vérifiez les autorisations.
3) Modifiez les fichiers suivants pour faire correspondre les chemins avec votre installation:
		- scripts/functions.php: l53, l57, l99, l125, l156, l159
		- expfiles/catalog : tout les chemins liés aux vues sont à modifier.
		
		si jamais les résultats n'apparaissent pas ou si des erreurs sont détectées lors de l'exécution, vérifiez
		que les chemins ont bien été changé.
4) Modifiez les wrappers. Les fichiers de mapping sont posés à l'extérieur du jar, pour permettre une modification
   simple de ce dernier en cas de changement au niveau du data source.
   Hotel: 	hotel.java en 79 et 85.
   DBpedia:	-
   Activite:wrapperV2.java en 74 et 80
   
   il faudra ensuite exporter chaque wrapper en jar exécutable et remplacer l'existant par ce nouveau!
   Si l'utilisation se fait au niveau de la fac, les wrappers sont à configurer pour le proxy, décommentez les lignes
   adéquates (au niveau du constructeur et de la main).
   
Si gros soucis, contactez sebastien.chenais@etu.univ-nantes.fr (06 04 05 45 19).
		