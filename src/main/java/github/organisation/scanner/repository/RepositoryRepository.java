package github.organisation.scanner.repository;

import github.organisation.scanner.ProcessGitHubOrganisation;
import github.organisation.scanner.model.Person;
import github.organisation.scanner.model.Repository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RepositoryRepository {

    private RestTemplate restTemplate = new RestTemplate();

    public List<Repository> getAllRepositories(String organisation) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + ProcessGitHubOrganisation.TOKEN);
        headers.add(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");
        HttpEntity request = new HttpEntity(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ProcessGitHubOrganisation.GITHUB)
                .queryParam("type", "all")
                .queryParam("role", "all")
                .queryParam("per_page", ProcessGitHubOrganisation.PAGES)
                .path("/orgs/{organisation}/repos");
        URI uri = builder.build().expand(organisation).encode().toUri();
        ParameterizedTypeReference<List<Map<String, Object>>> returnType = new ParameterizedTypeReference<List<Map<String, Object>>>(){};
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(uri, HttpMethod.GET, request, returnType);
        List<Map<String, Object>> all = response.getBody();
        return all.stream().map(this::processRepositoryMap).collect(Collectors.toList());
    }

    private Repository processRepositoryMap(Map<String, Object> repository) {
        Repository result = new Repository();
        //result.setId((int) Math.round(Double.parseDouble(repository.get("id").toString())));
        result.setId(Integer.parseInt(repository.get("id").toString()));
        result.setName(repository.get("name").toString());
        Optional.ofNullable(repository.get("description")).ifPresent(d -> result.setDescription(d.toString()));
        result.setFull_name(repository.get("full_name").toString());
        result.setOwner(((Map<String, String>)repository.get("owner")).get("login"));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "token " + ProcessGitHubOrganisation.TOKEN);
        headers.add(HttpHeaders.ACCEPT, "application/vnd.github.hellcat-preview+json");
        HttpEntity request = new HttpEntity(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ProcessGitHubOrganisation.GITHUB)
                .queryParam("affiliation", "all")
                .path("repos/{owner}/{repository}/collaborators");
        try {
            URI uri = builder.build().expand(result.getOwner(), result.getName()).encode().toUri();
            ParameterizedTypeReference<List<Person>> returnType = new ParameterizedTypeReference<List<Person>>(){};
            ResponseEntity<List<Person>> retrieved = restTemplate.exchange(uri, HttpMethod.GET, request, returnType);
            List<Integer> collaborators = retrieved.getBody().stream().map(Person::getId).collect(Collectors.toList());
            result.setCollaborators(collaborators);
        } catch (Exception notFound) {
            return result;
        }
        return result;
    }



}
