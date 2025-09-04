package util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

import model.Endereco;

public class ViaCepService {

    public static Endereco buscarEndereco(String cep) throws Exception {
        String cepLimpo = cep.replaceAll("\\D", "");
        if (cepLimpo.isBlank() || cepLimpo.length() != 8) {
            throw new IllegalArgumentException("CEP inválido");
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://viacep.com.br/ws/" + cepLimpo + "/json/"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());

        if (json.has("erro")) return null;

        Endereco endereco = new Endereco();
        endereco.setRua(json.optString("logradouro", ""));
        endereco.setBairro(json.optString("bairro", ""));
        endereco.setCidade(json.optString("localidade", ""));
        endereco.setEstado(json.optString("uf", ""));
        endereco.setCep(cepLimpo);

        return endereco;
    }
}
