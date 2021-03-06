package core.framework.impl.web.exception;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorResponse {
    @XmlElement(name = "id")
    public String id;

    @XmlElement(name = "severity")
    public String severity;

    @XmlElement(name = "error_code")
    public String errorCode;

    @XmlElement(name = "message")
    public String message;

    @XmlElement(name = "stack_trace")
    public String stackTrace;
}
