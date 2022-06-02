import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class Main {

    public static final String REMOTE_SERVICE_URI = "https://api.nasa.gov/planetary/apod?api_key=t0ud7kRidQFefE7t2baKlCq9jHaxakpgSSA4c7kq";

    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, URISyntaxException {

        // создание HttpClientBuilder
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();

        // создание объекта запроса с произвольными заголовками
        HttpGet request = new HttpGet(REMOTE_SERVICE_URI);

        // отправка запроса
        CloseableHttpResponse response = httpClient.execute(request);

        //Создадим в классе Main.java, json mapper

        //в методе main добавим код для преобразования json в java
        NASAClass nasaClassObj = mapper
                .readValue(response
                        .getEntity()
                        .getContent(), new TypeReference<>() {
                });

        String urlFile = nasaClassObj.getUrl();

        HttpGet requestDownload = new HttpGet(urlFile);
        CloseableHttpResponse responseDownload = httpClient.execute(requestDownload);
        HttpEntity entityDownload = responseDownload.getEntity();

        if (entityDownload != null) {
            //long len = entityDownload.getContentLength();
            InputStream inputStream = entityDownload.getContent();

            //получаем имя файла
            String fileName = getFilenameFromURL(urlFile);
            System.out.println(fileName);

            //создаём файл
            File resultFile = createFile(fileName);

            //запись в файл
            writeFile(inputStream, resultFile);
        }
    }

    public static void writeFile(InputStream inputStream, File resultFile) {
        System.out.println("запись файла файла...");

        try (OutputStream out = new BufferedOutputStream(
                new FileOutputStream(resultFile))) {

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
            System.out.println("файл загружен!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createFile(String fileName) {
        String path = new File("").getAbsolutePath();
        File myFileName = new File(path + "\\" + fileName);
        System.out.println("создание файла...");

        try {
            if (myFileName.createNewFile() || myFileName.exists()) {
                System.out.println("файл " + fileName + " создан в папке " + path);
            } else {
                System.out.println("ОШИБКА СОЗДАНИЯ ФАЙЛА " + fileName);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return myFileName;
    }

    public static String getFilenameFromURL(String url) throws URISyntaxException {
        System.out.println("получение имени файла...");
        var uri = new URI(url);
        return new File(uri.getPath()).getName();
    }
}


