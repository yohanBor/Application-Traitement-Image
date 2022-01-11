<template>
  <!-- Parcours et affiche toutes les images du serveur -->
  <div id="galerie">
      <img v-for="i in response" :key="i" :src="'images/'+i.id" :alt="i.name" :title="i.name" >
  </div>
</template>

<script>
import axios from "axios";
export default {
  name: "Galerie",
  props: {
    msg: String,
  },

  el: '#galerie',

  data() {
    return {
      response: [],
      errors: [],
    };
  },

  mounted () {
    this.callRestService();
  },

  methods: {
    callRestService() {
      axios
        .get('images')
        .then((response) => {
          this.response = response.data;
        })
        .catch((e) => {
          this.errors.push(e);
        });
    },
  },
};
</script>

<style scoped>

#galerie{
  padding: 20px;
  background: #42b983;
}

#galerie img{
  border: 2px solid black;
  display: block;
  margin : auto;
  margin-bottom: 20px;
  max-width: 500px;
}

</style>
