<template>
  <div class="galerie">
    <img alt="Vue logo" src="../assets/logo.png">
    <galerie-cpnent msg="Welcome to Your Vue.js App"/>
  </div>
</template>
<script>

// @ is an alias to /src
import GalerieCpnent from '@/components/GalerieCpnent.vue'

export default {
  name: 'Galerie',
  components: {
    GalerieCpnent
  }
}
</script>