package unit.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.controllers.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {
    @InjectMocks
    GlobalExceptionHandler globalExceptionHandler;

    @Mock
    BindingResult bindingResult;

    @Mock
    BindException bindException;

    @Mock
    MethodArgumentNotValidException argumentException;

    @Spy
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void handleBindException() {
        FieldError fieldError = new FieldError("testObject", "testField", "testMessage");

        when(bindException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleBindException(bindException, new HttpHeaders(),
                HttpStatus.UNPROCESSABLE_ENTITY, mock(WebRequest.class));

        assertEquals(responseEntity.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
        assertEquals(responseEntity.getBody(), "{\"testField\":\"testMessage\"}");
    }

    @Test
    public void handleBindException_JsonProcessingException() throws JsonProcessingException {
        FieldError fieldError = new FieldError("testObject", "testField", "testMessage");
        JsonProcessingException mockException = mock(JsonProcessingException.class);

        when(mockException.getMessage()).thenReturn("error");
        when(mapper.writeValueAsString(any())).thenThrow(mockException);
        when(bindException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleBindException(bindException, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(responseEntity.getBody(), "error");
    }

    @Test
    public void handleMethodArgumentNotValid() {
        FieldError fieldError = new FieldError("testObject", "testField", "testMessage");

        when(argumentException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleMethodArgumentNotValid(argumentException, new HttpHeaders(),
                HttpStatus.UNPROCESSABLE_ENTITY, mock(WebRequest.class));

        assertEquals(responseEntity.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
        assertEquals(responseEntity.getBody(), "{\"testField\":\"testMessage\"}");
    }

    @Test
    public void handleMethodArgumentNotValid_JsonProcessingException() throws JsonProcessingException {
        FieldError fieldError = new FieldError("testObject", "testField", "testMessage");
        JsonProcessingException mockItem = mock(JsonProcessingException.class);

        when(mockItem.getMessage()).thenReturn("error");
        when(mapper.writeValueAsString(any())).thenThrow(mockItem);
        when(argumentException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleMethodArgumentNotValid(argumentException, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(responseEntity.getBody(), "error");
    }
}
