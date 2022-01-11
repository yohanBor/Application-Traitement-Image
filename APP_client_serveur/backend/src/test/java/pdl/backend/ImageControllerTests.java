package pdl.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


//une requête HTTP multipart est une requête HTTP que les clients HTTP construisent pour envoyer des fichiers et des données 
//à un serveur HTTP

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	public static void reset() {
   // reset Image class static counter
   	ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));
	}

	@Test
	@Order(1)
	public void getImageListShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images/")).andDo(print()).andExpect(status().isOk());
	}

	@Test
    @Order(2)
    public void getImageShouldReturnNotFound() throws Exception {
        ImageDao imgDao=new ImageDao();
        List<Image> list=imgDao.retrieveAll();
        int size=list.size();
        this.mockMvc.perform(get("/images/"+size)).andExpect(status().isNotFound());
    }
	@Test
	@Order(3)
	public void getImageShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images/0")).andDo(print()).andExpect(status().isOk());
	}

	@Test
	@Order(4)
	public void deleteImageShouldReturnBadRequest() throws Exception {
		this.mockMvc.perform(delete("/image/test")).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@Order(5)
	public void deleteImageShouldReturnNotFound() throws Exception {
		ImageDao imgDao=new ImageDao();
        List<Image> list=imgDao.retrieveAll();
        int size=list.size();
		this.mockMvc.perform(delete("/images/"+size)).andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	@Order(6)
	public void deleteImageShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(delete("/images/0")).andDo(print()).andExpect(status().isOk());
	}

	@Test
	@Order(7)
	public void createImageShouldReturnSuccess() throws Exception {

		final ClassPathResource path = new ClassPathResource("img/"); //on se place dans le répertoire img

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file","test.jpg", "image/jpeg", path.getInputStream()); //on envoie une image au bon format (images/jpeg)
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
            .file(mockMultipartFile)).andExpect(status().isCreated()); //on attend le status ok
		
	}

	@Test
	@Order(8)
	public void createImageShouldReturnUnsupportedMediaType() throws Exception {

		final ClassPathResource path = new ClassPathResource("img/");

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file","test.jpg", "image/png", path.getInputStream());
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
            .file(mockMultipartFile)).andExpect(status().isUnsupportedMediaType());
			
	}

	//bonus : autres tests
	@Test
	@Order(9)
	public void getImageShouldReturnBadRequest() throws Exception {
		final ClassPathResource path = new ClassPathResource("img/");

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file","test.jpg", "image/png", path.getInputStream());
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images")
            .file(mockMultipartFile)).andExpect(status().isUnsupportedMediaType());
	}
	
}
