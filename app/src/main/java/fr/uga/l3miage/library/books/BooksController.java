
package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.data.domain.Book.Language;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.authors.AuthorMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.base.BaseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper) {
        this.bookService = bookService;
        this.booksMapper = booksMapper;
    }

    @GetMapping("/books")
    public Collection<BookDTO> books(@RequestParam(value = "q", required = false) String query) {
        Collection<Book> book;
        if (query == null) {
            book = bookService.list();
        } else {
            try {
                book = bookService.findByTitle(query);
            } catch (Exception e) {
                // TODO: handle exception
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
            }
        }
        return book.stream()
                .map(booksMapper::entityToDTO)
                .toList();
    }

    @GetMapping("/books/{booksId}")
    public ResponseEntity<BookDTO> book(@PathVariable("booksId") Long id) {
        Book book;
        try {
            book = bookService.get(id);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(booksMapper.entityToDTO(book), HttpStatus.OK);
    }

    @PostMapping("authors/{authorId}/books")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookDTO> newBook(@PathVariable("authorId") Long authorId, @RequestBody BookDTO book) {
        Book book2;
        try {
             book2 = booksMapper.dtoToEntity(book);
            long isbn = book2.getIsbn();
            short year = book2.getYear();
            long max = Long.valueOf("9999999999999");
            long min = Long.valueOf("1000000000");
            if (book2.getTitle().isBlank() || isbn > max || isbn < min || year < -9999 || year > 9999) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            if (book2.getLanguage() == null) {
                book2.setLanguage(Language.FRENCH);
            }
        } catch (Exception e) {
            // TODO: handle exception
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        try {
            bookService.save(authorId, book2);
            return new ResponseEntity<>(booksMapper.entityToDTO(book2), HttpStatus.CREATED);
        } catch (Exception e) {
            // TODO: handle exception
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("books/{bookId}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable("bookId") Long bookId, @RequestBody BookDTO book) {
        // attention BookDTO.id() doit être égale à id, sinon la requête utilisateur est
        // mauvaise
        try {
            Book book2 = booksMapper.dtoToEntity(book);
            if (book2.getId() != bookId) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            Book book3 = bookService.get(bookId);
            book3.setAuthors(book2.getAuthors());
            book3.setId(book2.getId());
            book3.setIsbn(book2.getIsbn());
            book3.setLanguage(book2.getLanguage());
            book3.setPublisher(book2.getPublisher());
            book3.setTitle(book2.getTitle());
            book3.setYear(book2.getYear());
            bookService.save(bookId, book3);
            return new ResponseEntity<>(booksMapper.entityToDTO(book3), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("books/{bookId}")
    public ResponseEntity<BookDTO> deleteBook(@PathVariable("bookId") Long id) {
        try {
            bookService.delete(id);
            return new ResponseEntity<>(null, HttpStatus.valueOf(204));
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("books/{bookId}/authors")
    public ResponseEntity<BookDTO> addAuthor(@PathVariable("bookId") Long bookId, @RequestBody AuthorDTO author) {
        try {
            Book book = bookService.get(bookId);
            // book.addAuthor(AuthorMapper.dtoToEntity(author));
            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
