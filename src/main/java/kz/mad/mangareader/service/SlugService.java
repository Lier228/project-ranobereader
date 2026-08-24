package kz.mad.mangareader.service;

import com.github.slugify.Slugify;
import org.springframework.stereotype.Service;

@Service
public class SlugService {

    private final Slugify slugify = Slugify.builder().lowerCase(true).build();

    public String createSlug(String input) {
        return slugify.slugify(input);
    }
}