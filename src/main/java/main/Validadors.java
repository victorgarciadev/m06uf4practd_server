package main;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 * @author GrupD
 */
public class Validadors {
    
    public static List<String> validaBean(Object b)
    {
        List<String> ret = new ArrayList<>();
        
        Configuration<?> config = Validation.byDefaultProvider().configure();
        
        ValidatorFactory factory = config.buildValidatorFactory();
        Validator validator = factory.getValidator();
            
        validator.validate(b).stream().forEach(x -> ret.add(x.getMessage()));
        
        return ret;
    }
    
}
