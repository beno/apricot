function Form(selector) {

  //this.form = $(selector);
  this.selector = selector;
  /* elementName => [{type, message[, value]}] */
  this.rules = {};
  this.validators = {
    "required": function(field, rule) {
        return field.value && field.value != "";
     },
     "regex": function(field, rule) {
        var regex = new RegExp(rule["value"]);
        return regex.test(field.value);
     },
     "sameAs": function (field, rule) {
       return field.value == field.form[rule["value"]].value;
     },
     "minlen": function (field, rule) {
       return field.value.length >= rule.value;
     },
     "maxlen": function (field, rule) {
       return field.value.length <= rule.value;
     },
     "min": function (field, rule) {
       var v = parseFloat(field.value);
       return !isNaN(v) && v >= rule.value;
     },
     "max": function (field, rule) {
       var v = parseFloat(field.value);
       return !isNaN(v) && v <= rule.value;
     },
     "integer": function (field, rule) {
       return !isNaN(parseInt(field.value));
     },
     "number": function (field, rule) {
       return !isNaN(parseFloat(field.value));
     }
  };

  this.addError = function(field, message) {
    var fobj = $(field);
    fobj.closest("div.clearfix").addClass("error");
    fobj.closest("div.input").append("<span class=\"error help-block\">"+message+"</span>");
    
  }

  this.removeError = function(field) {
    var fobj = $(field);
    var div = fobj.closest("div.clearfix");
    if (div.hasClass("error")) {
      div.removeClass("error");
      fobj.closest("div.input").children("span.error").remove();
    }
  }

  this.hasError = function(field) {
    return fobj.closest("div.clearfix").hasClass("error");
  }

  this.clearErrors = function() {
    var form = $(selector).get(0);
    for (i = 0; i < form.elements.length; i++) {
      this.removeError(form.elements[i]);
    }
  }

  this.addRule = function(key, rule) {
    var ar = this.rules[key];
    if (!ar) {
      ar = [];
      this.rules[key] = ar;
    }
    ar.push(rule);
  }

  this.addValidator = function(type, validator) {
    this.validators[type] = validator;
  }

  this.install = function(rules) {
    this.rules = rules;
    this.bind();
    return this;
  }

  this.bind = function() {
    var self = this;
    var form = $(selector);
    form.bind("submit", function(e) {
      if (!self.validate0(this)) {
        e.preventDefault();
      }
    });
    var domForm = form.get(0);
    for (var key in this.rules) {
      if (this.rules.hasOwnProperty(key)) {
        this.bindField(domForm[key]);
      }
    }
  }

  this.bindField = function(field) {
    var self = this;    
    $(field).change(function(e) {
      if (!self.validateField(this)) {
        e.preventDefault();
      }
    });
  }

  this.validate = function() {
    var form = $(this.selector).get(0);
    return this.validate0(form);
  }

  this.validate0 = function(form) {
    var isValid = true;
    for (var key in this.rules) {
      if (this.rules.hasOwnProperty(key)) {
        if (!this.validateField(form[key])) {
          isValid = false;
        }
      }
    }
    return isValid;
  }

  this.validateField = function(field) {
    this.removeError(field);
    var ar = this.rules[field.name];
    if (ar) {
      for (var i = 0; i<ar.length; i++) {
        var rule = ar[i];
        var validate = this.validators[rule["type"]];
        if (validate) {
          if (!validate(field, rule)) {
            this.addError(field, rule["message"]);
            return false;
          }
        }
      }
    }
    return true;
  }


};
