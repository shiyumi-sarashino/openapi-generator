/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.CodegenType;
import org.openapitools.codegen.DefaultCodegen;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.utils.ModelUtils;
import org.openapitools.codegen.InlineModelResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class HaskellServantStabCodegen extends DefaultCodegen implements CodegenConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(HaskellServantStabCodegen.class);

    // source folder where to write the files
    protected String sourceFolder = "src";
    protected String apiVersion = "0.0.1";
    private static final Pattern LEADING_UNDERSCORE = Pattern.compile("^_+");

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     */
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    public String getName() {
        return "haskell-stab";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    public String getHelp() {
        return "Generates a Haskell stab server library.";
    }

    private Map<String, ArrayList<String>> typeNameReplacements = new HashMap<>();

    public HaskellServantStabCodegen() {
        super();

        // override the mapping to keep the original mapping in Haskell
        specialCharReplacements.put("-", "Dash");
        specialCharReplacements.put(">", "GreaterThan");
        specialCharReplacements.put("<", "LessThan");

        // backslash and double quote need double the escapement for both Java and Haskell
        specialCharReplacements.remove("\\");
        specialCharReplacements.remove("\"");
        specialCharReplacements.put("\\\\", "Back_Slash");
        specialCharReplacements.put("\\\"", "Double_Quote");

        // set the output folder here
        outputFolder = "generated-code/haskell-servant-stab";

        /*
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        embeddedTemplateDir = templateDir = "haskell-servant-stab";

        /*
         * Api Package.  Optional, if needed, this can be used in templates
         */
        apiPackage = "API";

        /*
         * Model Package.  Optional, if needed, this can be used in templates
         */
        modelPackage = "Types";

        // Haskell keywords and reserved function names, taken mostly from https://wiki.haskell.org/Keywords
        setReservedWordsLowerCase(
                Arrays.asList(
                        // Keywords
                        "as", "case", "of",
                        "class", "data", "family",
                        "default", "deriving",
                        "do", "forall", "foreign", "hiding",
                        "if", "then", "else",
                        "import", "infix", "infixl", "infixr",
                        "instance", "let", "in",
                        "mdo", "module", "newtype",
                        "proc", "qualified", "rec",
                        "type", "where"
                )
        );

        /*
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);

        /*
         * Supporting Files.  You can write single files for the generator with the
         * entire object tree available.  If the input file has a suffix of `.mustache
         * it will be processed by the template engine.  Otherwise, it will be copied
         */
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("stack.mustache", "", "stack.yaml"));
        supportingFiles.add(new SupportingFile("Setup.mustache", "", "Setup.hs"));

        /*
         * Language Specific Primitives.  These types will not trigger imports by
         * the client generator
         */
        languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList(
                        "Bool",
                        "String",
                        "Int",
                        "Integer",
                        "Float",
                        "Char",
                        "Double",
                        "List",
                        "FilePath",
                        "Day",
                        "UTCTime"
                )
        );

        typeMapping.clear();
        typeMapping.put("array", "List");
        typeMapping.put("set", "Set");
        typeMapping.put("boolean", "Bool");
        typeMapping.put("string", "Text");
        typeMapping.put("integer", "Int");
        typeMapping.put("long", "Integer");
        typeMapping.put("short", "Int");
        typeMapping.put("char", "Char");
        typeMapping.put("float","Float");
        typeMapping.put("double", "Double");
        typeMapping.put("DateTime", "UTCTime");
        typeMapping.put("Date", "Day");
        typeMapping.put("file", "FilePath");
        typeMapping.put("binary", "FilePath");
        typeMapping.put("number", "Double");
        typeMapping.put("any", "Value");
        typeMapping.put("UUID", "Text");
        typeMapping.put("ByteArray", "Text");
        typeMapping.put("object", "Value");

        importMapping.clear();
        importMapping.put("Map", "qualified Data.Map as Map");

        cliOptions.add(new CliOption(CodegenConstants.MODEL_PACKAGE, CodegenConstants.MODEL_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.API_PACKAGE, CodegenConstants.API_PACKAGE_DESC));
    }

    // @Override
    // public void processOpts() {
    //     super.processOpts();

    //     if (StringUtils.isEmpty(System.getenv("HASKELL_POST_PROCESS_FILE"))) {
    //         LOGGER.info("Hint: Environment variable HASKELL_POST_PROCESS_FILE not defined so the Haskell code may not be properly formatted. To define it, try 'export HASKELL_POST_PROCESS_FILE=\"$HOME/.local/bin/hfmt -w\"' (Linux/Mac)");
    //     }
    // }

    /**
     * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
     * those terms here.  This logic is only called if a variable matches the reserved words
     *
     * @return the escaped term
     */
    @Override
    public String escapeReservedWord(String name) {
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    public String firstLetterToUpper(String word) {
        if (word.length() == 0) {
            return word;
        } else if (word.length() == 1) {
            return word.substring(0, 1).toUpperCase(Locale.ROOT);
        } else {
            return word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1);
        }
    }

    public String firstLetterToLower(String word) {
        if (word.length() == 0) {
            return word;
        } else if (word.length() == 1) {
            return word.substring(0, 1).toLowerCase(Locale.ROOT);
        } else {
            return word.substring(0, 1).toLowerCase(Locale.ROOT) + word.substring(1);
        }
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        // From the title, compute a reasonable name for the package and the API
        String title = openAPI.getInfo().getTitle();

        // Drop any API suffix
        if (title == null) {
            title = "OpenAPI";
        } else {
            title = title.trim();
            if (title.toUpperCase(Locale.ROOT).endsWith("API")) {
                title = title.substring(0, title.length() - 3);
            }
        }

        String[] words = title.split(" ");

        // The package name is made by appending the lowercased words of the title interspersed with dashes
        List<String> wordsLower = new ArrayList<String>();
        for (String word : words) {
            wordsLower.add(word.toLowerCase(Locale.ROOT));
        }
        String cabalName = joinStrings("-", wordsLower);

        // The API name is made by appending the capitalized words of the title
        List<String> wordsCaps = new ArrayList<String>();
        for (String word : words) {
            wordsCaps.add(firstLetterToUpper(word));
        }
        String apiName = joinStrings("", wordsCaps);

        // Set the filenames to write for the API
        supportingFiles.add(new SupportingFile("haskell-servant-codegen.mustache", "", cabalName + ".cabal"));
        supportingFiles.add(new SupportingFile("API.mustache", "src/" + apiName, "API.hs"));
        supportingFiles.add(new SupportingFile("Types.mustache", "src/" + apiName, "Types.hs"));
        supportingFiles.add(new SupportingFile("Main.mustache", "src/", "Main.hs"));


        additionalProperties.put("title", apiName);
        additionalProperties.put("titleLower", firstLetterToLower(apiName));
        additionalProperties.put("package", cabalName);

        // Due to the way servant resolves types, we need a high context stack limit
        additionalProperties.put("contextStackLimit", openAPI.getPaths().size() * 2 + 300);



        List<Map<String, Object>> replacements = new ArrayList<>();
        Object[] replacementChars = specialCharReplacements.keySet().toArray();
        for (int i = 0; i < replacementChars.length; i++) {
            String c = (String) replacementChars[i];
            Map<String, Object> o = new HashMap<>();
            o.put("char", c);
            o.put("replacement", "'" + specialCharReplacements.get(c));
            o.put("hasMore", i != replacementChars.length - 1);
            replacements.add(o);
        }
        additionalProperties.put("specialCharReplacements", replacements);

        if(openAPI.getComponents() != null
                && openAPI.getComponents().getSchemas() != null
                && openAPI.getPaths() != null){
              Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
             if (openAPI.getPaths() != null) {
                 for (String pathname : openAPI.getPaths().keySet()) {
                     PathItem path = openAPI.getPaths().get(pathname);
                     if (path.readOperations() != null) {
                         for (Operation operation : path.readOperations()){
                             makeTypeNameReplacements(operation, schemas, openAPI);
                         }
                     }
                 }
             }
             if (openAPI.getComponents().getResponses() != null){
                    Map<String, ApiResponse> apiResponses = openAPI.getComponents().getResponses();
                    List<Map<String, Object>> status = new ArrayList<>();
                    for (String key : apiResponses.keySet()) {
                        ApiResponse apiResponse = apiResponses.get(key);
                        status.add(errResp2status(apiResponse, schemas, openAPI));
                    }
                    additionalProperties.put("status", status);
              }
        }
        super.preprocessOpenAPI(openAPI);
    }


    private String descriptionToType(String desc){
        List<String> ss = new ArrayList<>(Arrays.asList(desc.split(" ")));
        Integer size = ss.size();
        for(Integer i=0; i<size; i++){
            if(ss.get(i).equals("-TypeName") && ss.get(i+1) != null){
                return firstLetterToUpper(ss.get(i+1));
            }
        }
        return "";
    }

    private String descriptionToErrType(String desc){
        List<String> ss = new ArrayList<>(Arrays.asList(desc.split(" ")));
        Integer size = ss.size();
        String errType = "";
        for(Integer i=0; i<size; i++){
            if(ss.get(i).equals("-ErrType") && ss.get(i+1) != null){
                errType = ss.get(i+1);
                if(errType.equals("ad-hoc") && ss.get(i+2) != null){
                    errType = ss.get(i+2);
                }
                break;
            }
        }
        return firstLetterToUpper(errType);
    }

    /**
     * Optional - type declaration.  This is a String which is used by the templates to instantiate your
     * types.  There is typically special handling for different property types
     *
     * @return a string value used as the `dataType` field for model templates, `returnType` for api templates
     */
    @Override
    public String getTypeDeclaration(Schema p) {
        if (ModelUtils.isArraySchema(p)) {
            ArraySchema ap = (ArraySchema) p;
            Schema inner = ap.getItems();
            return "[" + getTypeDeclaration(inner) + "]";
        } else if (ModelUtils.isMapSchema(p)) {
            Schema inner = ModelUtils.getAdditionalProperties(p);
            return "Map.Map String " + getTypeDeclaration(inner);
        }
        return fixModelChars(super.getTypeDeclaration(p));
    }

    /**
     * Optional - OpenAPI type conversion.  This is used to map OpenAPI types in a `Schema` into
     * either language specific types via `typeMapping` or into complex models if there is not a mapping.
     *
     * @return a string value of the type or complex model for this property
     */
    @Override
    public String getSchemaType(Schema p) {
        String schemaType = super.getSchemaType(p);
        LOGGER.debug("debugging swager type: " + p.getType() + ", " + p.getFormat() + " => " + schemaType);
        String type = null;
        if (typeMapping.containsKey(schemaType)) {
            type = typeMapping.get(schemaType);
            //if (languageSpecificPrimitives.contains(type))
            //    return toModelName(type);
        } else if (typeMapping.containsValue(schemaType)) {
            type = schemaType + "_";
        } else {
            type = schemaType;
        }
        return toModelName(type);
    }

    @Override
    public String toInstantiationType(Schema p) {
        if (ModelUtils.isMapSchema(p)) {
            Schema additionalProperties2 = ModelUtils.getAdditionalProperties(p);
            String type = additionalProperties2.getType();
            if (null == type) {
                LOGGER.error("No Type defined for Additional Property " + additionalProperties2 + "\n" //
                        + "\tIn Property: " + p);
            }
            String inner = getSchemaType(additionalProperties2);
            return "(Map.Map Text " + inner + ")";
        } else if (ModelUtils.isArraySchema(p)) {
            ArraySchema ap = (ArraySchema) p;
            String inner = getSchemaType(ap.getItems());
            // Return only the inner type; the wrapping with QueryList is done
            // somewhere else, where we have access to the collection format.
            return "[" + inner + "]";
        } else {
            return null;
        }
    }


    // Intersperse a separator string between a list of strings, like String.join.
    private String joinStrings(String sep, List<String> ss) {
        StringBuilder sb = new StringBuilder();
        for (String s : ss) {
            if (sb.length() > 0) {
                sb.append(sep);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    // Convert an HTTP path to a Servant route, including captured parameters.
    // For example, the path /api/jobs/info/{id}/last would become:
    //      "api" :> "jobs" :> "info" :> Capture "id" IdType :> "last"
    // IdType is provided by the capture params.
    private List<String> pathToServantRoute(String path, List<CodegenParameter> pathParams) {
        // Map the capture params by their names.
        HashMap<String, String> captureTypes = new HashMap<String, String>();
        for (CodegenParameter param : pathParams) {
            captureTypes.put(param.baseName, param.dataType);
        }

        // Cut off the leading slash, if it is present.
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // Convert the path into a list of servant route components.
        List<String> pathComponents = new ArrayList<String>();
        for (String piece : path.split("/")) {
            if (piece.startsWith("{") && piece.endsWith("}")) {
                String name = piece.substring(1, piece.length() - 1);
                pathComponents.add("Capture \"" + name + "\" " + captureTypes.get(name));
            } else {
                pathComponents.add("\"" + piece + "\"");
            }
        }

        // Intersperse the servant route pieces with :> to construct the final API type
        return pathComponents;
    }

    private List<String> pathToFuncType(String path, List<CodegenParameter> params) {
        // Map the capture params by their names.
        HashMap<String, String> captureTypes = new HashMap<String, String>();
        for (CodegenParameter param : params) {
            captureTypes.put(param.baseName, param.dataType);
        }

        // Cut off the leading slash, if it is present.
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // Convert the path into a list of servant route components.
        List<String> capType = new ArrayList<String>();
        for (String piece : path.split("/")) {
            if (piece.startsWith("{") && piece.endsWith("}")) {
                String name = piece.substring(1, piece.length() - 1);
                String captureType = captureTypes.get(name);
                capType.add(captureType);
            }
        }

        // Intersperse the servant route pieces with :> to construct the final API type
        return capType;
    }

    private String descriptionToErrTypeIIFnotAdHoc(String desc){
        List<String> ss = new ArrayList<>(Arrays.asList(desc.split(" ")));
        Integer size = ss.size();
        String errType = "ad-hoc";
        for(Integer i=0; i<size; i++){
            if(ss.get(i).equals("-ErrType") && ss.get(i+1) != null){
                errType = ss.get(i+1);
                break;
            }
        }
        return firstLetterToUpper(errType);
    }

    private Map<String, Object> errResp2status(ApiResponse apiResponse,Map<String, Schema> schemas, OpenAPI openAPI){
        Map<String, Object> o = new HashMap<>();
        Schema schema = new Schema();
        if( apiResponse.getDescription()!=null
                && apiResponse.getContent()!=null
                && apiResponse.getContent().get("application/json")!=null
                && apiResponse.getContent().get("application/json").getSchema()!=null){
            Schema json = apiResponse.getContent().get("application/json").getSchema();
            List<String> sd = new ArrayList<>();
            Integer size = 0;
            String errType = "";
            String statusCode = "";
            if(json.get$ref()!=null){
                String ref = json.get$ref();
                if(typeNameReplacements.get(ref) != null){
                    errType = typeNameReplacements.get(ref).get(1);
                }
                ArrayList<String> refls = new ArrayList(Arrays.asList(ref.split("/")));
                ref = refls.get(refls.size() - 1).toString();
                json = schemas.get(ref);
            }
            if(apiResponse.getDescription() != null){
                sd = Arrays.asList(apiResponse.getDescription().split("\\s+|\\n+"));
                size = sd.size();
                errType = descriptionToErrType(apiResponse.getDescription());
            }
            if(!sd.contains("-StatusCode")){
                LOGGER.error("common err resporses' description must have -StatusCode.");
            }else if(openAPI != null){
                for(Integer i=0; i<size; i++){
                    if(sd.get(i).equals("-StatusCode") && sd.get(i+1) != null){
                        statusCode = sd.get(i+1);
                        try
                        {
                            Integer stcd = Integer.parseInt(statusCode);
                            List<Integer> scs = (List<Integer>) additionalProperties.get("statusCode");
                            Boolean orgn = true;
                            if(scs != null){
                                for(Integer sc : scs){
                                    if(stcd.equals(sc)){
                                        orgn = false;
                                        break;
                                    }
                                }
                                if(orgn){
                                    scs.add(stcd);
                                }
                                additionalProperties.put("statusCode", scs);
                            }else{
                                additionalProperties.put("statusCode", new ArrayList<Integer>(Arrays.asList(stcd)));
                            }
                        }
                        catch (NumberFormatException ex)
                        {
                            LOGGER.error(statusCode + ": -StatusCode should be status code number.");
                        }
                        break;
                    }
                }
            }
            o.put("name", firstLetterToUpper(errType));
            o.put("statusCode", statusCode);

            final Map<String, Schema> propertyMap = json.getProperties();
            if(propertyMap!=null){
                ArrayList<Schema> ss = new ArrayList<>(propertyMap.values());
                //get schema from description in #/components/responses/{keyName}/content/schema/properties/{firstItem}
                schema = ss.get(0);
            }
        }
        if(schema != null && schema.getExample() != null){
            o.put("errMessage", schema.getExample().toString().replace("\n", "\\n").replace("\\\"", "\\\\\"").replace("\"", "\\\""));
        }
        return o;
    }

    private String ref2name(String ref){
        ArrayList<String> refls = new ArrayList(Arrays.asList(ref.split("/")));
        return refls.get(refls.size() - 1).toString();
    }
    private void makeTypeNameReplacements(Operation operation, Map<String, Schema> schemas, OpenAPI openAPI){
          if (operation.getTags() != null){
              List<String> tags = new ArrayList<String>();
              for (String word : operation.getTags()) {
                  tags.add(firstLetterToUpper(word));
              }
              String tag = joinStrings("", tags);
              operation.addExtension("x-tags", tag);
          }
          if (operation.getOperationId()!=null){
              String opId = firstLetterToUpper(operation.getOperationId());
              Map<String, ApiResponse> resps = operation.getResponses();
              Set<String> resKeys = resps.keySet();
              for(String key : resKeys){
                  ApiResponse resp = resps.get(key);
                  String type = "";
                  Boolean errp = false;
                  if(Integer.parseInt(key)/100 == 2){
                      type = "-TypeName ";
                      errp = false;
                  }else{
                      type = "-ErrType ";
                      errp = true;
                  }

                  if(resp != null
                          && resp.getContent()!=null
                          && resp.getContent().get("application/json")!=null
                          && resp.getContent().get("application/json").getSchema()!=null){
                      Schema schm = resp.getContent().get("application/json").getSchema();
                      String ref = resp.getContent().get("application/json").getSchema().get$ref();
                      ArrayList<String> typeName = new ArrayList<String>();
                      if(schm instanceof ArraySchema){
                          ArraySchema arraySchema = (ArraySchema) schm;
                          if (arraySchema.getItems().get$ref() != null){
                            ref = arraySchema.getItems().get$ref();
                          }
                      }else if(resp.getContent().get("application/json").getSchema().get$ref()==null){
                          ref = resp.getContent().get("application/json").getSchema().get$ref();
                      }
                      if(resp.getDescription() != null
                          && resp.getDescription().contains(type)){
                          if(errp){
                              String errType = descriptionToErrTypeIIFnotAdHoc(resp.getDescription());
                              if(errType.equals("ad-hoc")){
                                  errType = descriptionToErrType(resp.getDescription());
                                  typeName = new ArrayList<String>(Arrays.asList("ad-hoc", errType));
                              }else{
                                  typeName = new ArrayList<String>(Arrays.asList("Err", errType));
                              }
                          } else {
                              typeName = new ArrayList<String>(Arrays.asList("Res", descriptionToType(resp.getDescription())));
                          }
                      } else {
                          if(errp){
                              typeName = new ArrayList<String>(Arrays.asList("ad-hoc", "Err" + opId));
                          }else{
                              typeName = new ArrayList<String>(Arrays.asList("Res", "Res" + opId));
                          }
                      }
                      if (ref != null
                              && !typeNameReplacements.keySet().contains(ref)
                              && ref.contains("/inline_")){
                          typeNameReplacements.put(ref, typeName);
                      }
                  }
              }
              if(operation.getRequestBody() != null){
                  RequestBody s = operation.getRequestBody();
                  String ref = s.get$ref();
                  ArrayList<String> typeName = new ArrayList<String>();
                  if( operation.getRequestBody().getContent()!=null
                          && operation.getRequestBody().getContent().get("application/json")!=null
                          && operation.getRequestBody().getContent().get("application/json").getSchema()!=null
                          && operation.getRequestBody().getContent().get("application/json").getSchema().get$ref()!=null){
                      ref = s.getContent().get("application/json").getSchema().get$ref();
                  }
                  if(s.getDescription() != null
                          && s.getDescription().contains("-TypeName ")){
                      typeName = new ArrayList<String>(Arrays.asList("Req" , descriptionToType(s.getDescription())));
                  } else {
                      typeName = new ArrayList<String>(Arrays.asList("Req", "ReqBody" + opId));
                  }
                  if (ref != null
                          && !typeNameReplacements.keySet().contains(ref)
                          && ref.contains("/inline_")){
                      typeNameReplacements.put(ref, typeName);
                  }
              }
          }
          Map<String, ApiResponse> apiResponses = openAPI.getComponents().getResponses();
          List<Map<String, Object>> status = new ArrayList<>();
          for (String key : apiResponses.keySet()) {
              ApiResponse apiResponse = apiResponses.get(key);
              status.add(errResp2status(apiResponse, schemas, null));
          }
          additionalProperties.put("status", status);
    }
    @Override
    public CodegenOperation fromOperation(String resourcePath, String httpMethod, Operation operation, Map<String, Schema> definitions, OpenAPI openAPI) {
        CodegenOperation op = super.fromOperation(resourcePath, httpMethod, operation, definitions, openAPI);

        List<String> path = pathToServantRoute(op.path, op.allParams);
        List<String> func = pathToFuncType(op.path, op.allParams);
        List<Boolean> args = new ArrayList<Boolean>();
        Map<String, Schema> schemas = new HashMap<String, Schema>();
        if(openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null){
            schemas = openAPI.getComponents().getSchemas();
        }


        for(Integer i = 0; i < func.size(); i++){
            args.add(true);
        }

        // Query parameters appended to routes
        for (CodegenParameter param : op.queryParams) {
            String paramType = param.dataType;
            path.add("QueryParam \"" + param.baseName + "\" " + paramType);
            func.add("Maybe " + paramType);
            args.add(true);
        }

        // Either body or form data parameters appended to route
        // As far as I know, you cannot have two ReqBody routes.
        // Is it possible to have body params AND have form params?
        String bodyType = null;
        if (op.getHasBodyParam()) {
            for (CodegenParameter param : op.bodyParams) {
                bodyType = param.dataType;
                func.add(bodyType);
                args.add(true);
                path.add("ReqBody '[JSON] " + bodyType);
            }
        } else if(op.getHasFormParams()) {
            // Use the FormX data type, where X is the conglomerate of all things being passed
            bodyType = "Form" + camelize(op.operationId);
            func.add(bodyType);
            args.add(true);
            path.add("ReqBody '[FormUrlEncoded] " + bodyType);
        }

        // Special headers appended to route
        for (CodegenParameter param : op.headerParams) {
            path.add("Header \"" + param.baseName + "\" " + param.dataType);
            String paramType = param.dataType;
            func.add("Maybe " + paramType);
            args.add(true);
        }

        // Add the HTTP method and return type
        String returnType = op.returnType;
        if (returnType == null || returnType.equals("null")) {
            returnType = "()";
        }
        if (returnType.indexOf(" ") >= 0) {
            returnType = "(" + returnType + ")";
        }

        List<String> errStatus = new ArrayList<>();
        List<Map<String, Object>> adhocStatus = new ArrayList<>();
        List<Integer> adStatusCode = new ArrayList<>();
        boolean not2xx = false;
        boolean jsonEx = false;
        ApiResponses resps = operation.getResponses();

        for(String key : resps.keySet()){
            ApiResponse resp = resps.get(key);
            if(resp.getContent()!=null
                    && resp.getContent().get("application/json")!=null
                    && resp.getContent().get("application/json").getSchema()!=null){
                Schema s = resp.getContent().get("application/json").getSchema();
                boolean arrayp = false;
                if (s instanceof ArraySchema) {
                    ArraySchema as = (ArraySchema) s;
                    s = as.getItems();
                    arrayp = true;
                }
                if(s.get$ref()!=null){

                    String ref = s.get$ref();
                    ArrayList<String> type = typeNameReplacements.get(ref);
                    ArrayList<String> refls = new ArrayList(Arrays.asList(ref.split("/")));
                    String refName = refls.get(refls.size() - 1).toString();
                    s = schemas.get(refName);
                    if(type != null){
                        if(type.get(0).equals("ad-hoc") || type.get(0).equals("Err")){
                            not2xx = true;
                            if(type.get(0).equals("ad-hoc")){
                                List<Integer> lastcs = (List<Integer>) additionalProperties.get("statusCode");
                                // and add to additionalProperties
                                adhocStatus.add(errResp2status(resp, schemas, null));
                                List<Integer> scs = (List<Integer>) additionalProperties.get("statusCode");
                                for(Integer sc : scs.subList(lastcs.size(),scs.size())){
                                    adStatusCode.add(sc);
                                }
                            }
                            path.add("Throws " + camelize(fixModelChars(type.get(1))));
                            errStatus.add(camelize(fixModelChars(type.get(1))));
                        } else {
                            returnType = type.get(1);
                            if(arrayp){
                                returnType = "[" + returnType + "]";
                            }
                        }
                    }
                    if(s.getExample() != null){
                        Object ex = s.getExample();
                        String example = returnType + makeExEnvelope(ex);
                        ObjectMapper mapper = new ObjectMapper();
                        try{
                            example = mapper.writeValueAsString(ex);
                        } catch (JsonProcessingException e) {
                            example =null;
                            e.printStackTrace();
                        } catch (IOException e) {
                            example =null;
                            e.printStackTrace();
                        }
                        if(example==null || example.equals("")){
                            op.vendorExtensions.put("x-example", "pureSuccEnvelope ()");
                        }else{
                            op.vendorExtensions.put("x-example", "pureEnvelope . decode . fromString $ \"" + example.replace("\n", "\\n").replace("\\\"", "\\\\\"").replace("\"", "\\\"") + "\"");
                            if(arrayp){
                                op.vendorExtensions.put("x-example", "pureEnvelope . decode .fromString $ \"[" + example.replace("\n", "\\n").replace("\\\"", "\\\\\"").replace("\"", "\\\"") + "]\"");
                            }
                            jsonEx = true;
                        }
                    }else if(!jsonEx){
                        op.vendorExtensions.put("x-example", "pureSuccEnvelope ()");
                    }
                }
            }
        }
        if(jsonEx){
            op.vendorExtensions.put("x-example-type", "Maybe " + returnType + " -> " + "Handler (Envelope '" + errStatus.toString() + " " + returnType + ")");
        }
        op.vendorExtensions.put("x-ad-hocStatus", adhocStatus);
        op.vendorExtensions.put("x-additionalStatusCode", adStatusCode);

        if(!not2xx){
            path.add("NoThrow");
        }

        path.add("Verb '" + op.httpMethod.toUpperCase() + " " + op.responses.get(0).code + " '[JSON] " + returnType);
        func.add("Handler (Envelope '" + errStatus.toString() + " " + returnType + ")");

        List<Boolean> opIdLs = new ArrayList<Boolean>(Arrays.asList(new Boolean[op.operationId.length()]));
        op.vendorExtensions.put("x-opId-size", opIdLs);
        op.vendorExtensions.put("x-funcs", joinStrings(" -> ", func));
        op.vendorExtensions.put("x-errStatus", errStatus.get(0));
        op.vendorExtensions.put("x-args", args);
        op.vendorExtensions.put("x-routeType", joinStrings(" :> ", path));
        op.vendorExtensions.put("x-formName", "Form" + camelize(op.operationId));
        for(CodegenParameter param : op.formParams) {
            param.vendorExtensions.put("x-formPrefix", camelize(op.operationId, true));
        }

        return op;
    }

    private String makeExEnvelope(Object ex){
        if (ex instanceof String){
            return " \"" + ex.toString().replace("\n","\\n") + "\"";
        }else if (ex instanceof Integer
                || ex instanceof Double) {
            return " " + ex.toString();
        }else if (ex instanceof Boolean) {
            if ((Boolean) ex){
                return " True";
            }else{
                return " False";
            }
        }

        String example = "";
        Object child = ex;
        List<Object> next = new ArrayList<Object>();
        List<String> listHead = new ArrayList<String>();
        List<String> tail = new ArrayList<String>();
        next.add(ex);
        Boolean break2 = false;
        while(next.size() != 0){
            child = next.get(next.size()-1);
            next.remove(next.size()-1);
            while(child != null){
                Integer nextsz=next.size();
                if(child instanceof HashMap){
                    List<String> done = new ArrayList<String>();
                    Map<String, Object> childMap = (Map<String, Object>) child;
                    for(String key : childMap.keySet()){
                        done.add(key);
                        if (childMap.get(key) instanceof Object[]) {
                            ArrayList<Object> childls = new ArrayList<Object>(Arrays.asList((Object[]) childMap.get(key)));
                            Map<String, Object> rest = new HashMap<String, Object>();
                            for(String restKey : childMap.keySet()){
                                if(!done.contains(restKey)){
                                    rest.put(restKey, childMap.get(restKey));
                                }
                            }
                            next.add(rest);
                            next.add(childls.subList(1, childls.size()));
                            listHead.add(", ");
                            tail.add("]");
                            example = example + " " + camelize(key) + " [";
                            child = childls.get(0);
                            break2 = true;
                            break;
                        }else if (childMap.get(key) instanceof HashMap) {
                            Map<String, Object> rest = new HashMap<String, Object>();
                            for(String restKey : childMap.keySet()){
                                if(!done.contains(restKey)){
                                    rest.put(restKey, childMap.get(restKey));
                                }
                            }
                            next.add(rest);
                            tail.add("}");
                            example = example + " " + camelize(key) + " {";
                            child = childMap.get(key);
                            break2 = true;
                            break;
                        } else {
                            example = example + makeExEnvelope_(" " + camelize(key) + " =", childMap.get(key));
                        }
                    }
                } else if (child instanceof Object[]){
                    ArrayList<Object> childls = new ArrayList<Object>(Arrays.asList((Object[]) child));
                    for (Object el : childls){
                        if (el instanceof Object[]) {
                            next.add(childls.subList(1, childls.size()));
                            example = example + listHead.get(listHead.size()-1) + "[";
                            listHead.add(camelize(", "));
                            tail.add("]");
                            child = el;
                            break2 = true;
                            break;
                        }else if (el instanceof HashMap) {
                            next.add(childls.subList(1, childls.size()));
                            example = example + listHead.get(listHead.size()-1) + " { ";
                            child = childls.get(0);
                            tail.add("}");
                            break2 = true;
                            break;
                        } else {
                            example = example + "[" + listHead.get(listHead.size()-1) + makeExEnvelope_(listHead.get(tail.size()-1), el) + "]";
                        }
                    }
                }
                if (next.size() == nextsz){
                    child = null;
                }
            }
            if(!break2){
                break2 = false;
                if(next.size() > 2){
                String tailBracket = tail.remove(tail.size() -1);
                example = example + tailBracket;
                    if(tailBracket.equals("]")){
                        listHead.remove(listHead.size() -1);
                    }
                }
            }
        }
        return example;
    }

    private String makeExEnvelope_ (String pref, Object child){
        if (child instanceof String){
            return pref + " \"" + child.toString().replace("\n","\\n") + "\"";
        }else if (child instanceof Integer
                || child instanceof Double) {
            return pref + " " + child.toString();
        }else if (child instanceof Boolean) {
            if ((Boolean) child){
                return pref + " True";
            }else{
                return pref + " False";
            }
        }
        return pref + "";
    }

    private String makeQueryListType(String type, String collectionFormat) {
        type = type.substring(1, type.length() - 1);
        switch (collectionFormat) {
            case "csv":
                return "(QueryList 'CommaSeparated (" + type + "))";
            case "tsv":
                return "(QueryList 'TabSeparated (" + type + "))";
            case "ssv":
                return "(QueryList 'SpaceSeparated (" + type + "))";
            case "pipes":
                return "(QueryList 'PipeSeparated (" + type + "))";
            case "multi":
                return "(QueryList 'MultiParamArray (" + type + "))";
            default:
                throw new UnsupportedOperationException();
        }
    }

    private String fixOperatorChars(String string) {
        StringBuilder sb = new StringBuilder();
        String name = string;
        //Check if it is a reserved word, in which case the underscore is added when property name is generated.
        if (string.startsWith("_")) {
            if (reservedWords.contains(string.substring(1, string.length()))) {
                name = string.substring(1, string.length());
            } else if (reservedWordsMappings.containsValue(string)) {
                name = LEADING_UNDERSCORE.matcher(string).replaceFirst("");
            }
        }
        for (char c : name.toCharArray()) {
            String cString = String.valueOf(c);
            if (specialCharReplacements.containsKey(cString)) {
                sb.append("'");
                sb.append(specialCharReplacements.get(cString));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Remove characters from a string that do not belong in a model classname
    private String fixModelChars(String string) {
        return string.replace(".", "").replace("-", "");
    }

    // Override fromModel to create the appropriate model namings
    @Override
    public CodegenModel fromModel(String name, Schema schema, Map<String, Schema> allSchemas) {
        CodegenModel model = super.fromModel(name, schema, allSchemas);
        ArrayList<String> type = typeNameReplacements.get("#/components/schemas/" + name);
        if (type == null){
            type = typeNameReplacements.get("#/components/requestBodies/" + name);
        }
        if(type != null){
            model.classname = type.get(1);
            if(type.get(0).equals("Err") || type.get(0).equals("ad-hoc ")){
                model.vendorExtensions.put("x-errType", true);
            }
        }

        if(schema instanceof ArraySchema){
            model.vendorExtensions.put("x-arr", true);
        }
        // Clean up the class name to remove invalid characters
        model.classname = camelize(fixModelChars(model.classname));
        if(typeMapping.containsValue(model.classname)) {
            model.classname += "_";
        }

        // From the model name, compute the prefix for the fields.
        String prefix = camelize(model.classname, true);
        for(CodegenProperty prop : model.vars) {
            if(prop.allowableValues != null) {
                for(Integer i = 0; i < prop._enum.size(); i++){
                    prop._enum.set(i, fixOperatorChars(prop._enum.get(i)));
                }
            }
            prop.name = toVarName(prefix + camelize(fixOperatorChars(prop.name)));
            ArrayList<String> propType = typeNameReplacements.get("#/components/schemas/" + prop.getDatatype());
            if(propType != null){
                prop.setDatatype(propType.get(1));
            }else{
                propType = typeNameReplacements.get("#/components/schemas/" + firstLetterToLower(prop.getDatatype()));
                if(propType != null){
                    prop.setDatatype(propType.get(1));
                }else{
                    prop.setDatatype(camelize(fixModelChars(prop.getDatatype())));
                }
            }
            String nameUpper = firstLetterToUpper(fixOperatorChars(prop.name));
            prop.vendorExtensions.put("x-nameUpper", nameUpper);
        }

        // Create newtypes for things with non-object types
        // check if it's a ModelImpl before casting
        //if (!(schema instanceof ModelImpl)) {
        //    return model;
        //}

        //String modelType = ((ModelImpl)  schema).getType();
        String modelType = schema.getType();
        if(!"object".equals(modelType) && typeMapping.containsKey(modelType)) {
            String newtype = typeMapping.get(modelType);
            model.vendorExtensions.put("x-customNewtype", newtype);
        }

        // Provide the prefix as a vendor extension, so that it can be used in the ToJSON and FromJSON instances.
        model.vendorExtensions.put("x-prefix", prefix);

        return model;
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("{-", "{_-").replace("-}", "-_}");
    }

    @Override
    public void postProcessFile(File file, String fileType) {
        if (file == null) {
            return;
        }
        String haskellPostProcessFile = System.getenv("HASKELL_POST_PROCESS_FILE");
        if (StringUtils.isEmpty(haskellPostProcessFile)) {
            return; // skip if HASKELL_POST_PROCESS_FILE env variable is not defined
        }

        // only process files with hs extension
        if ("hs".equals(FilenameUtils.getExtension(file.toString()))) {
            String command = haskellPostProcessFile + " " + file.toString();
            try {
                Process p = Runtime.getRuntime().exec(command);
                int exitValue = p.waitFor();
                if (exitValue != 0) {
                    LOGGER.error("Error running the command ({}). Exit value: {}", command, exitValue);
                } else {
                    LOGGER.info("Successfully executed: " + command);
                }
            } catch (Exception e) {
                LOGGER.error("Error running the command ({}). Exception: {}", command, e.getMessage());
            }
        }
    }
}
