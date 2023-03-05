package fr.uga.l3miage.library.authors;

import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.library.books.BookDTO;
import fr.uga.l3miage.library.books.BooksMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;


@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AuthorsController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;
    private final BooksMapper booksMapper;

    @Autowired
    public AuthorsController(AuthorService authorService, AuthorMapper authorMapper, BooksMapper booksMapper) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
        this.booksMapper = booksMapper;
    }

    @GetMapping("/authors")
    public Collection<AuthorDTO> authors(@RequestParam(value = "q", required = false) String query) {
        Collection<Author> authors;
        if (query == null) {
            authors = authorService.list();
        } else {
            try {
                authors = authorService.searchByName(query);
            } catch (Exception e) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
            }
        }
        return authors.stream()
                .map(authorMapper::entityToDTO)
                .toList();
    }

    @GetMapping("/authors/{authorId}")
    public ResponseEntity<AuthorDTO> author(@PathVariable("authorId") Long id) {
        Author author;
        try {
            author = authorService.get(id);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(authorMapper.entityToDTO(author), HttpStatus.OK);
    }

    @PostMapping("/authors")
    public ResponseEntity<AuthorDTO> newAuthor(@RequestBody AuthorDTO author) {
        Author author2;
        try {
            author2 = authorMapper.dtoToEntity(author);
            if(author2.getFullName().isBlank()) {
                return new ResponseEntity<>(null, HttpStatus.valueOf(400));
            }
            authorService.save(author2);
            return new ResponseEntity<>(authorMapper.entityToDTO(author2), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/authors/{id}")
    public ResponseEntity<AuthorDTO> updateAuthor(AuthorDTO author, @PathVariable("id") Long id) {
        // attention AuthorDTO.id() doit être égale à id, sinon la requête utilisateur
        // est mauvaise
        Author author2;
        try {
            author2 = authorMapper.dtoToEntity(author);
            if (author2.getId() != id) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            Author author3 = authorService.get(id);

            author3.setBooks(author2.getBooks());
            author3.setFullName(author2.getFullName());
            //authorService.update(author3);
            return new ResponseEntity<>(authorMapper.entityToDTO(author3), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/authors/{id}")
    public ResponseEntity<AuthorDTO> deleteAuthor(@PathVariable("id")  Long id) {
        // unimplemented... yet!
        Author author;
        try {
            author = authorService.get(id);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        try{
            Set<Book> books = author.getBooks();
            for (Book book : books) {
                if(book.getAuthors().size()>1){
                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                }                
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
            try {
            authorService.delete(id);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(null, HttpStatus.valueOf(204));
    }

    @GetMapping("/authors/{authorId}/books")
    public ResponseEntity<Collection<BookDTO>> books(@PathVariable("authorId") Long authorId) {
        try {
            Set<Book> books = authorService.get(authorId).getBooks();
            return new ResponseEntity<>(booksMapper.entityToDTO(books), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

}
