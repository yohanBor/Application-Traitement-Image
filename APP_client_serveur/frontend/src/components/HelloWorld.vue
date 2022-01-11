<script type="text/javascript" src="../http-api.js"></script>  

<template>
  <div class="hello">
    <p id ="bienvenue"> Bienvenue dans cette application web dédiée au traitement d'image. Pour télécharger une image située sur le serveur (avant/après) traitement
      cliquer sur l'image. Bonne navigation ! 
    </p>
    
    <div class="select-image">
      <!-- Accès possible aux images par identifiants / par nom de fichier -->
       <p>Type d'accès aux images : </p>
          <input type="radio" @click="callRestService()" id = "identifiant" name="remarque" value="news"/> Par identifiant<br />
          <input type="radio" @click="callRestService()" id = "fichier" name="remarque" value="commentaire"/> Par nom de fichier<br />
          <input type="radio" @click="callRestService()" id = "taille" name="remarque" value="size"/> Par taille<br />
          <input type="radio" @click="callRestService()" id = "metadata_all" name="remarque" value="metadata"/> Visualiser toutes les métadonnées<br/>
      <!-- Change l'image affiché en fonction de la selection -->
      <select @click="getImage()">
        <!-- Affichage par defaut -->
        <option disabled selected value="0">Selectionnez votre image</option>
         <!-- Affichage des images presentes sur le serveur -->
        <option v-for="i in response " :key="i" :value="i.id" >{{ Form_Images(i) }}</option>
      </select>

      <figure>
      <!-- Image affichée en fonction de l'image selectionnée -->
        <a href="" id="img_dl" download="imagepastraite">
         <img class="imageEl" src=""  >
        </a>
        <div class = "metadata">
          <figcaption id = "meta_images"> </figcaption>
        </div>
      </figure>
      
      <!-- Supprimer une image du serveur --> 
       <button class="delete_picture" @click="Delete_Image()">Supprimer l'image sélectionnée </button>
    </div>

    <img class="imageEl" src="" >


    <!-- Filtre image -->
    <div class="formFilter">
      <h1>Filtre d'image</h1>
      <select @click="affiche()" id="filterselect">
        <option selected value="ColorToGray">Colorer en gris</option>
        <option value="HueFilter"> Teinter l'image</option>
        <option value="contrast">Contraste </option>
        <option value="Ehistogramme"> Egalisation d'histogramme</option>
        <option value="brighness"> Modification de luminosité</option>
        <option value="gauss"> Flou</option>
        <option value="edges"> Edges</option>
        <option value="sharpen"> Sharpen</option>
        <option value="negative"> Negative</option>
        <option value="reverse"> Retourner</option>
      </select>

      <div id = "param1">
       <p> Valeur de teinte : </p>
       <input type="range" id = "range" min="0" max="359" step="1.0" 
          onchange="document.getElementById('param').innerHTML=this.value" />
         <strong id="param">10</strong> 
      </div>
      
       <div id = "param_min_max">
         <p> Valeur minimum : </p>
          <input type="range" id = "range_minimum" min="0" max="255" step="1.0" 
          onchange="document.getElementById('range_min').innerHTML=this.value" />
          <strong id="range_min">10</strong>
         <p> Valeur maximum : </p>
          <input type="range" id = "range_maximum" min="0" max="255" step="1.0" 
          onchange="document.getElementById('range_max').innerHTML=this.value" />
          <strong id="range_max">10</strong> 
        </div>

      <div id = "param_lum">
         <p> Valeur : </p>
          <input type="range" id = "range_lum" min="-255" max="255" step="1.0" 
          onchange="document.getElementById('luminosite').innerHTML=this.value" />
          <strong id="luminosite">10</strong>
      </div>

      <div id = "flou">
          <input type="radio"  id = "middle" value="0" name="flou"/> Moyen<br />
          <input type="radio"  id = "gauss" value="1" name="flou"/> Gaussien<br />
      </div>

      <button v-on:click="filterIMG()">Appliquer le filtre</button>

      <p id="message_traitement"></p>
      <a href="" id="img_filter_dl" download="imagetraité"> <img class="image_after_filtre" src="" id = "img_filter"> </a>    
    </div>

    <!-- Ajout d'image sur le serveur -->
    <div class="add-image">
          <input type="file" id="file" ref="file" v-on:change="addImage()"/>
          <button v-on:click="submitImage()">ajouter</button>
    </div>
  </div>

  
</template>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

.metadata {
  
  margin-left: auto;
  margin-right: auto;
  width: 615px;
}

figure {
    border: thin #c0c0c0 solid;
    display: flex;
    flex-flow: column;
    padding: 5px;
    background-color: #8f8f8fa4;
    width: 90%;
    padding: 15px;
    margin: auto;
}

.hello{
  background: rgb(180, 180, 180);
  padding-top: 40px;
  padding-bottom: 40px;
}
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}

.image_after_filtre {
  width: 50%;
}

figcaption {
    background-color: #42b983;
    color: #fff;
    text-align: center;
    font: italic smaller sans-serif;
    font-size: 15px;
    padding: 5px;
    border-radius: 5px;
}

/* CSS Ajouté */

.select-image, .formFilter, .hello #bienvenue{
  background: white;
  width: 60%;
  margin: auto;
  margin-bottom: 20px;
  border : solid 2px black;
  border-radius: 4px;
  padding: 40px;
}
.select-image select {
  background: #42b983;
  font-size: 15px;
  text-transform: uppercase;
  margin : auto;
  margin-bottom: 20px;
  padding: 10px;
  display: block;
  width: 60%;
}

.select-image .imageEl{
  display: block;
  margin-right : auto;
  margin-left : auto;
  margin-bottom: 20px;
  max-width: 60%;
}

.select-image .delete_picture, .formFilter button{
  margin-top: 20px;
  padding: 10px;
}

/* Ajout image */
.add-image{
  background: rgb(197, 197, 197);
  width: 60%;
  margin : auto;
  margin-top : 20px;
  padding: 40px;
}

.add-image input[type=file]{
  width: 40%;
  display: block;
  margin: auto;
  margin-bottom: 20px;
  background: #999999;
  padding: 20px;
  color : white;
  text-align: center;
  border : 2px solid #8f8f8f;
}

.add-image button{
  width: 120px;
  display: block;
  margin: auto;
  background: #42b983;
  padding: 20px;
  text-align: center;
  text-transform: uppercase;
  color : black;
  font-size: 14px;
  font-weight: bold;
  border : 1px solid #8f8f8f;
  transition: 0.4s;
  cursor: pointer;
}
.add-image button:hover{
 color : white;
 background: black;
 transition: 0.4s;
 transform: scale(1.05);
}

.formFilter select{
  margin: auto;
  display: block;
  width: 50%;
  padding: 15px;
  margin-bottom: 10px;
  background: #42b983;
  font-size: 14px;
}

</style>
