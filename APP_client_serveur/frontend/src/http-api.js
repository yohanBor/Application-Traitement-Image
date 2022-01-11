import axios from "axios";

export default {
  name: "HelloWorld",
  //  Contient un tableau d'object de proprieté specifique à Vue.js
  props: {
    msg: String,
  },

  // Selecteur CSS du DOM, lié a la 'Vue Component', ici c'est la classe "hello"
  el: '.hello',

  // Fonction qui retourne l'object data, qui represente la donnée interne de la 'Vue Component'
  data() {
    return {
      response: [],
      errors: [],
      file: '' // image en attente d'être envoyé en POST au serveur
    };
  },
  
  // Création de l'instance 'component', interaction avec la data ... mais pas d'accés au DOM
 created(){
    // alert('Created');
  },

  // Le 'component' est compilé mais pas encore affiché à l'écran
  beforeMount (){
    // alert('BeforeMount');
  },

  // Ajoute des noeuds au DOM quand la page est chargée, on peut maintenant utiliser les contenus de la page (interactif)
  mounted () {
    // alert('Mounted');
    this.callRestService();
    this.getImage();
    this.affiche();
  },

  methods: {
    filterIMG(){
      document.getElementById("message_traitement").innerHTML = "<span id='test'>Traitement de l'image en cours ...</span>";

      var after = document.getElementById("img_filter");
      var after_dl = document.getElementById("img_filter_dl");
      //var imageElement = this.$el.querySelector("img");
      var selects = this.$el.querySelector("select");
      var selectedImage = selects.options[selects.selectedIndex].value;
      var filter = document.getElementById("filterselect");
      var selectedFilter = filter.options[filter.selectedIndex].value;
      var param1=document.getElementById("range").value;
      if (document.getElementById("filterselect").value == "contrast"){
        param1 = document.getElementById("range_minimum").value;
      }
      if (document.getElementById("filterselect").value == "brighness"){
        param1 = document.getElementById("range_lum").value;
      }
      var param2=document.getElementById("range_maximum").value;
      if (document.getElementById("filterselect").value == "contrast"){
        if (param2 < param1){
          alert("Veuillez saisir une valeur minimum inférieure à celle du maximum");
        }     
      }

      if (document.getElementById("filterselect").value == "gauss"){
        if (document.getElementById("gauss").checked){
          param1=1
          console.log("gauss")
        }
        else
          param1=0
      }
      
      axios.get("images/"+selectedImage+"?algorithm="+selectedFilter+"&arg1="+param1+"&arg2="+param2, { responseType:"blob" }) 
      .then(function (response) {
        var reader = new window.FileReader();
        reader.readAsDataURL(response.data); 
        reader.onload = function() {
            var imageDataUrl = reader.result;
            after.setAttribute("src", imageDataUrl);
            after_dl.setAttribute("href", imageDataUrl);
            
            document.getElementById("message_traitement").innerHTML = "<span id='test'>...Terminé !</span>";
        }
      });
    },


    affiche(){
      document.getElementById("param1").style.display = "none"; 
      document.getElementById("param_min_max").style.display = "none"; 
      document.getElementById("param_lum").style.display = "none"; 
      document.getElementById("flou").style.display = "none";

      if (document.getElementById("filterselect").value == "HueFilter")
        document.getElementById("param1").style.display = "block";
      
      else if (document.getElementById("filterselect").value == "contrast")
        document.getElementById("param_min_max").style.display = "block";

      else if (document.getElementById("filterselect").value == "brighness")
        document.getElementById("param_lum").style.display = "block";

      else if (document.getElementById("filterselect").value == "gauss")
        document.getElementById("flou").style.display = "block";
    },

    // Rempli la data en fonction de la 'liste d'image' presente coté serveur
    callRestService() {
      
      axios
        .get('images') // Recupere la liste coté serveur 
        .then((response) => { 
          // JSON responses are automatically parsed.
          this.response = response.data; // remplie la data (response[])
        })
        .catch((e) => {
          this.errors.push(e);
        });
    },
 
    
    // Rempli la source "src" de l'image selectionnée (dans le fichier HelloWorld.vue)
    getImage() {
      var imageElement = this.$el.querySelector("img"); // Position sur la balise "img"
      var selects = this.$el.querySelector("select");
      var selectedImage = selects.options[selects.selectedIndex].value; // Recupere la 'value' de l'image selectionnée
      var dlid=document.getElementById("img_dl");
      axios.get('images/'+selectedImage, { responseType:"blob" }) 
      .then(function (response) {
          var reader = new window.FileReader();
          reader.readAsDataURL(response.data); 
          reader.onload = function() {
              var imageDataUrl = reader.result;
              imageElement.setAttribute("src", imageDataUrl); // Rempli la source de l'image (qui permet donc un affichage 
                                                              // de l'image selectionner en fonction de sa presence sur la bdd du serveur)
              dlid.setAttribute("href",imageDataUrl);

              document.getElementById("meta_images").innerHTML = "Identifiant de l'image : " + selectedImage;

              var x = document.getElementById("size");
              x.innerHTML = "un autre texte"  
          }
      });
    },

    test_id(option){
      return option.size;
    },

    // Ajoute l'image à 'file' en recuperant la ref de l'image ajoutée par l'utilisateur
    addImage(){
      this.file = this.$refs.file.files[0];
    },

    // Ajoute l'image JPEG choisis par l'utilisateur sur le serveur, dans la bdd 'images'
    submitImage(){
      // Prepare la donnée à envoyer au serveur
      let formData = new FormData();
      formData.append('file', this.file);

      // Execute la requete
      axios.post( '/images',
        formData,
        {
          headers: {
              'Content-Type': 'multipart/form-data'
          }
        }
      ).then(function(){
        alert("Image ajoutée"); // Indication pour utilisateur
        document.location.href="/"; // Mise à jour de la page

      })
      .catch(function(){
        alert("Votre image n'a pas pu être ajoutée"); // Indication pour utilisateur
      });
    },
    //supprime une image du serveur
    Delete_Image() {
      
      var selects = this.$el.querySelector("select");
      var selectedImage = selects.options[selects.selectedIndex].value; // Recupere la 'value' de l'image selectionnée
      axios.delete('images/'+selectedImage, { responseType:"blob" }) 
      document.location.href="/"; // on reload la page pour que cela soit effectif 
    },

    Form_Images(option) {
      
      if (document.getElementById("fichier").checked)
        return option.name;
      if (document.getElementById("identifiant").checked)
        return option.id;
      if (document.getElementById("taille").checked){
        return option.size;
      }
      if (document.getElementById("metadata_all").checked){
        return option;
      }
      return option.name;
    },
  },
};