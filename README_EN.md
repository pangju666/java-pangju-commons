[中文](README.md) | [English](README_EN.md)

<p align="center">
<a href="https://github.com/pangju666/java-pangju-common/releases">
<img alt="GitHub release" src="https://img.shields.io/github/release/pangju666/java-pangju-common.svg?style=flat-square&include_prereleases" />
</a>

<a href="https://central.sonatype.com/search?q=g:io.github.pangju666%20%20a:pangju-common-bom&smo=true">
<img alt="maven" src="https://img.shields.io/maven-central/v/io.github.pangju666/pangju-common-bom.svg?style=flat-square">
</a>

<a href="https://www.apache.org/licenses/LICENSE-2.0">
<img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
</a>
</p>

# Pangju Commons Tool Library

[[_TOC_]]

## Project Introduction

A collection of Java tool library extensions based on Apache Commons, jasypt, tika, twelvemonkeys, poi-tl, hanlp, gson,
reflections and other well-known third-party libraries, providing commonly used tool classes in development.
The project is designed in a modular manner, with each module focusing on a specific functional area.

## Start quickly

Dependency management

```xml
<!-- BOM Dependency Management -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.pangju666</groupId>
            <artifactId>pangju-commons-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

        <!-- using individual modules -->
<dependency>
<groupId>io.github.pangju666</groupId>
<artifactId>pangju-commons-lang</artifactId>
</dependency>
```

## Build Instructions

```bash
# build the whole project
mvn clean install

# building individual modules
mvn -pl pangju-commons-lang clean install
 ```

## Module Description

### 🛠️ pangju-commons-lang (common tool module)

Common tool modules, providing basic tool class collections.

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-lang</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **PinyinComparator**: Pinyin-based string comparator
    - **Function**:
        - Provide the ability to sort Chinese strings in pinyin order
        - Implementing an immutable Comparator interface
        - Supports custom pinyin separators
    - **Sorting rules**:
        - null value has the highest priority
        - Empty string ""
        - Blank string " " again
        - Other strings are sorted in pinyin order
    - **Core Features**:
        - Automatically identify ASCII printable characters and Chinese characters
        - High-quality pinyin conversion using HanLP
        - Provide static tools and methods to simplify sorting operations
        - Supports two collection types: List and array sorting
    - **Example**:
        - Before sorting: "How is the weather", null, " ", "
        - After sorting: null, "", " ", "How is the weather"


- **SystemClock**: High-performance system clock tool class (copyed from mybatis-plus)
    - **Function**:
        - Optimize the performance problem of System.currentTimeMillis() in high concurrency scenarios
        - Provides a millisecond-level system current timestamp acquisition method
        - Reduce system calls by timed update of timestamps through background threads
    - **Core Features**:
        - **High performance**: Compared to calling System.currentTimeMillis() directly, the performance is
          significantly improved.
        - **Low overhead**: Use single-threaded timed updates, and the resource consumption is very small
        - **Automatic recycling**: The thread is automatically recycled when the JVM exits, without manual cleaning
        - **Thread safety**: Based on AtomicLong implementation, ensure concurrency safety
    - **Performance comparison**:
        - 1 billion calls: 210.7% increase
        - 100 million calls: 162.0% increase
        - 10 million calls: 40.0% increase
        - 1 million calls: 5.0% increase
    - **How ​​to use**:
        - Directly call the static method SystemClock.now() to get the current time stamp


- **DataUnit**: Standard data size unit enumeration (copyed from Spring framework)
    - **Function**:
        - Provide standardized data size unit definitions
        - Support parsing corresponding enum values ​​from unit suffixes
        - Used with DataSize class for data size representation
    - **Supporting unit**:
        - **BYTES**: Byte(B), 2^0, 1 byte
        - **KILOBYTES**: Kilobytes (KB), 2^10, 1,024 bytes
        - **MEGABYTES**: Megabytes (MB), 2^20, 1,048,576 bytes
        - **GIGABYTES**: Gigabytes (GB), 2^30, 1,073,741,824 bytes
        - **TERABYTES**: Terabytes (TB), 2^40, 1,099,511,627,776 bytes
    - **Core Method**:
        - **fromSuffix(String)**: Get the corresponding DataUnit enum based on the unit suffix
        - **size()**: Get the DataSize instance corresponding to the current unit
    - **Technical Features**:
        - Use Binary Prefix notation
        - Enumeration design facilitates type-safe unit conversion
        - Provide friendly exception information processing


- **RegexFlag**: Regex pattern flag enumeration
    - **Function**:
        - Encapsulate flag bit constants of Java regular expression Pattern class
        - Provide enumeration type-safe flag access method
        - Simplify regular expression pattern configuration
    - **Support flag**:
        - **UNIX_LINES**: Enable Unix line mode, only '\n' is recognized as the line ending character
        - **CASE_INSENSITIVE**: Enable case-insensitive matching
        - **COMMENTS**: Allows the use of blanks and comments in regular expressions
        - **MULTILINE**: Enable multi-line mode to change the matching behavior of ^ and $
        - **DOTALL**: Enable single-line mode to match all characters including line ending characters
        - **UNICODE_CASE**: Enable Unicode-aware case collapse
        - **CANON_EQ**: Enable specification equivalence matching
    - **Core Method**:
        - **getValue()**: Get the integer value of the corresponding flag bit for pattern compilation
    - **Usage scenarios**:
        - Build advanced regular expression matching patterns
        - Configuring regular expression engine behavior
        - Handle multilingual text matching
        - Precise control of regular expression matching rules


- **gson**: JSON serialization and deserialization support
    - **Function**:
        - Provides the ability to convert between various Java types and JSON
        - Supports custom serialization and deserialization of common data types
        - Seamless integration with Gson library
    - **Serializer**:
        - **ClassJsonSerializer**: Serialize Class objects into fully qualified class name strings
        - **DateJsonSerializer**: Serializes the Date object to timestamp (milliseconds)
        - **LocalDateJsonSerializer**: Convert LocalDate object to timestamp
        - **LocalDateTimeJsonSerializer**: Convert LocalDateTime object to timestamp
    - **Deserializer**:
        - **BigDecimalDeserializer**: Support parsing BigDecimal from strings
        - **BigIntegerDeserializer**: Support parsing BigInteger from strings
        - **ClassJsonDeserializer**: Restore Class object from class name string
        - **DateJsonDeserializer**: Restore Date object from timestamp or time string representation
        - **LocalDateJsonDeserializer**: Restore LocalDate object from timestamp or time string representation
        - **LocalDateTimeJsonDeserializer**: Restore LocalDateTime object from timestamp or time string representation
    - **Technical Features**:
        - Unified time type processing strategy (converted to timestamp)
        - Numerical type conversion with strong fault tolerance
        - Type-safe serialization and deserialization
        - Work in conjunction with JsonUtils tool class


- **NanoId**: Small, secure, URL friendly unique string ID generator (copyed from hu-tool)
    - **Function**:
        - Generate compact, secure random string identifiers
        - Provide customizable ID lengths and character sets
        - Supports custom random number generator
    - **Core Features**:
        - **Security**: Use encryption-level random API to ensure correct symbol allocation
        - **Small size**: Only 258 bytes (after compression), no external dependencies
        - **Compactness**: Use more symbol sets than UUID (A-Za-z0-9_-)
        - **URL-friendly**: The generated ID can be used directly for the URL without encoding
    - **Main Method**:
        - **randomNanoId()**: Generate NanoID with default length (21 characters)
        - **randomNanoId(int)**: Generate NanoID of the specified length
        - **randomNanoId(Random, char[], int)**: Completely customize the generated parameters
    - **Technical implementation**:
        - Provide cryptographic security based on SecureRandom
        - Optimize random character selection using bit operations
        - The algorithm complexity is O(1), and the performance is stable


- **SnowflakeIdWorker**: Snowflake Algorithm ID Generator (Chat GPT Generated)
    - **Function**:
        - Generate globally unique 64-bit long integer ID
        - Support efficient ID generation in distributed environments
        - Provides thread-safe ID acquisition mechanism
    - **ID Structure**:
        - **Symbol bit (1 bit)**: Always 0, not used
        - **Timestamp (41 bits)**: It can be used for about 69 years relative to the number of milliseconds of the start
          time
        - **Data Center ID (5 bits)**: Supports 32 data centers
        - **Machine ID (5 bits)**: Each data center supports 32 machines
        - **Serial number (12 bits)**: 4096 IDs can be generated per millisecond
    - **Core Features**:
        - **High performance**: Single machine can generate millions of IDs per second
        - **Clock callback processing**: Detect and reject the clock callback situation
        - **Custom configuration**: Supports custom start timestamps and sequence numbers
        - **Bit operation optimization**: Use bit operations to improve generation efficiency


- **DataSize**: Data size representation class (copyed from Spring framework)
    - **Function**:
        - Representation and operation data size in bytes as the base unit
        - Support parsing data size from strings (such as "12MB")
        - Provides conversion between different data units
        - Immutable and thread-safe design
    - **Core Features**:
        - **Unit support**: Supports bytes (B), kilobytes (KB), megabytes (MB), gigabytes (GB), and terabytes (TB)
        - **Create method**: Provides multiple static factory method creation examples
        - **String parsing**: Supports string parsing from "5MB", "12KB", etc.
        - **Comparison operation**: Implement the Comparable interface and support size comparison
    - **Main Method**:
        - **ofBytes/ofKilobytes/ofMegabytes/ofGigabytes/ofTerabytes**: Create the data size of the specified unit
        - **parse(CharSequence)**: parse data size from a string
        - **toBytes/toKilobytes/toMegabytes/toGigabytes/toTerabytes**: Convert to different units
        - **isNegative()**: Check whether it is a negative value
    - **Technical Features**:
        - Based on binary prefix (1KB=1024B) instead of decimal prefix
        - Parsing string representations using regular expressions
        - Supports custom default units
        - Provide complete equals and hashCode implementations


- **TreeNode**: Tree structure node common interface
    - **Function**:
        - Basic operations for defining tree data structures
        - Support generic node identification and business data
        - Provide core methods for building tree structures
    - **Core Features**:
        - **Generic support**: Flexible definition of node key type (K) and node data type (T)
        - **Standardized interface**: Operation method of unified tree structure
        - **Simple design**: Only the necessary core methods of tree structure
    - **Main Method**:
        - **getNodeKey()**: Get the node unique identifier key
        - **getParentNodeKey()**: Get the parent node's unique identifier key
        - **setChildNodes(Collection<T>)**: Set the child node collection


- **Constants**: Commonly used constant collection classes
    - **Function**:
        - Provide centralized management of various common constants
        - Organize constant definitions by function classification
        - Reduce magic value usage in the project
    - **Constant Classification**:
        - **Date time constant**:
            - Standard date and time format (yyyy-MM-dd HH:mm:ss)
            - Standard date format (yyyy-MM-dd)
            - Standard time format (HH:mm:ss)
        - **Character-related constant**:
            - Chinese character range ('\u4e00'~'\u9fa5')
            - Number character range ('0'~'9')
            - Alphabetical character range ('A'~'Z', 'a'~'z')
            - Underscore character ('_')
        - **Network-related constants**:
            - HTTP/HTTPS protocol prefix
            - HTTP Success Status Code (200)
            - Path separator ('/')
            - Token prefix ("Bearer")
            - Local IP address (IPv4/IPv6)
        - **JSON related constants**:
            - Empty JSON object("{}")
            - Empty JSON array("[]")
        - **File related constants**:
            - General MIME type ("*/*")
        - **Reflection-related constants**:
            - Method prefix (get/set)
            - CGLIB agent related identification
        - **XML related constants**:
            - XML special character escape (, & etc.)


- **RegExPool**: Regex Constant Pool
    - **Function**:
        - Provide centralized management of various commonly used regular expressions
        - Contains rich predefined regular expression constants
        - Supports various data verification scenarios
    - **Regular Classification**:
        - **Basic Characters**:
            - Match basic characters such as letters, numbers, Chinese characters, etc.
            - Hexadecimal color code matching
        - **Network-related**:
            - IPv4/IPv6 address matching
            - URL/domain/email address verification
            - HTTP/FTP/File URL format verification
        - **Dict number**:
            - ID number
            - Passport number
            - License plate number
            - Unified Social Credit Code
        - **Numerical format**:
            - Integer/floating point number
            - Currency amount (supports negative numbers and thousandths)
            - Version number format
        - **File path**:
            - Windows/Linux file path
            - File name (with/excluding extension)
        - **Time Date**:
            - Date format (YYYY-MM-DD)
            - 12/24 hour time


- **random**: Random Data Generation Toolkit
    - **Function**:
        - Provide a variety of random data generation tools
        - Supports secure and non-secure random number generation
        - Supports random array and list generation of basic data types
    - **Main Category**:
        - **RandomArray**: Random array generation tool class
            - Supports generation of basic types of arrays such as boolean, integer, long integer, floating point
              number, etc.
            - Provides normal random arrays and element-unique random array generation
            - Supports random array generation with specified range of values
            - Provide three random strategies: insecure (non-secure), secure (secure), secureStrong (strong security)
        - **RandomList**: Random list generation tool class
            - Supports generating lists of basic types such as boolean, integer, long integer, floating point number,
              etc.
            - Provides normal random list and element unique random list generation
            - Supports random list generation of specified numerical ranges
            - Share the same three random strategies as RandomArray


- **ArrayUtils**: Array operation tool class
    - **Function**:
        - Inherited and extended the ArrayUtils functionality of Apache Commons Lang
        - Provides array segmentation function, splitting array into subarray lists according to specified size
        - Supports all basic type arrays and generic array operations
        - Thread-safe method implementation
    - **Core Features**:
        - **Segmentation function**: Supports dividing arrays of various types into subarray lists of specified sizes
        - **Full types support**: Override boolean, byte, char, double, float, int, long, short and generic arrays
        - **Border processing**: Automatically handles the situation where the last subarray may be smaller than the
          specified size
        - **Null value security**: Provides safe handling of empty arrays and illegal parameters
    - **Main Method**:
        - **partition(boolean[], int)**: Split boolean array
        - **partition(byte[], int)**: Split byte array
        - **partition(char[], int)**: Split character array
        - **partition(double[], int)**: Split double precision floating point array
        - **partition(float[], int)**: Split single-precision floating point array
        - **partition(int[], int)**: Split an array of integers
        - **partition(long[], int)**: Split long integer array
        - **partition(short[], int)**: Split a short integer array
        - **partition(T[], int)**: Split generic arrays


- **DateFormatUtils**: Date Format Tool Class
    - **Function**:
        - Inherited and extended Apache Commons Lang's DateFormatUtils functionality
        - Provide common date and time formatting methods
        - Supports null value safe processing
    - **Core Features**:
        - **Standard format**: Use predefined standard formats (yyyy-MM-dd HH:mm:ss, etc.)
        - **Multiple types support**: Support formatting of Date objects and timestamps (Long)
        - **Null value security**: Safe processing of null value and return empty string
        - **Convenient method**: Provides a method without parameters to obtain the formatted result of the current time
    - **Main Method**:
        - **formatDatetime()**: FormatDatetime()**: Format the current date and time to "yyyy-MM-dd HH:mm:ss"
        - **formatDatetime(Date)**: FormatDate as datetime
        - **formatDatetime(Long)**: Format the timestamp to date time
        - **formatDate()**: Format the current date to "yyyy-MM-dd"
        - **formatDate(Date)**: FormatDate as a date
        - **formatDate(Long)**: Format the timestamp to date
        - **formatTime(Date)**: FormatDetermine Date as time "HH:mm:ss"
        - **formatTime(Long)**: Format the timestamp to time


- **DateUtils**: DateTime Tool Class
    - **Function**:
        - Inherited and extended Apache Commons Lang's DateUtils functionality
        - Provides comprehensive functions such as date analysis, conversion, and calculation
        - Support Java 8 date time API and traditional Date transfer
        - Provide rich time difference calculation methods
    - **Core Features**:
        - **Date parsing**: Supports parsing strings into Date objects in multiple formats
        - **Type conversion**: Supports Date, LocalDate, LocalDateTime, timestamp and other types of conversion
        - **Default value mechanism**: All methods support null value safe processing and default values
        - **Time difference calculation**: Provides time difference calculation for various particle sizes such as
          milliseconds, seconds, minutes, hours, and days.
        - **Truncated calculation**: Supports truncated difference calculation for fields such as year, month, day,
          hour, minute, and seconds.
    - **Main Method Classification**:
        - **Analysis method**: parseDate, parseDateOrDefault, etc.
        - **Conversion method**: toDate, toLocalDate, toLocalDateTime, getTime, etc.
        - **Time difference calculation**: betweenMillis, betweenSeconds, betweenMinutes, etc.
        - **Truncated calculation**: truncateBetweenYears, truncateBetweenMonths, etc.
        - **Practical tools**: calculateAge, nowDate, etc.


- **DesensitizationUtils**: Data desensitization processing tool class
    - **Function**:
        - Comply with Alibaba's desensitization rules and standards
        - Provides desensitization methods for a variety of sensitive data
        - Supports custom desensitization rules and policies
    - **Core Features**:
        - **Dissile ID number**: ID card, officer certificate, passport and other ID number desensitization
        - **Contact information desensitization**: Desensitization of mobile phone number, landline phone, email address
        - **Personal information desensitization**: Desensitization of personal information such as name, address,
          nickname, etc.
        - **Vehicle information desensitization**: Vehicle information desensitization such as license plate number,
          engine number, frame number, etc.
        - **Financial information desensitization**: Bank card number, password and other financial information
          desensitization
        - **Medical Insurance Social Security Desensitization**: Social Security Card Number, Medical Insurance Card
          Number and other information desensitization
    - **Desensitization strategy**:
        - **Left reserve***: Reserve the number of digits on the left side of the string, and fill it with an asterisk
          on the right side.
        - **Reserve on the right**: Keep the specified number of digits on the right of the string, fill the left with
          an asterisk
        - **Ring retain**: Reserve the number of digits specified at the beginning and end of the string, and fill it
          with an asterisk in the middle.
        - **Hide all**: Replace the entire string with an asterisk


- **IdCardUtils**: ID card number processing tool class
    - **Function**:
        - Provide ID number verification function
        - Supports parsing gender information from ID number
        - Supports parsing date of birth from ID number
    - **Core Features**:
        - **Number Verification**: Supports legality verification of 18-digit ID number
        - **Check code verification**: Implement the national standard ID card verification code algorithm
        - **Date verification**: Verify whether the date of birth in the ID card is legal
        - **Multiple formats support**: Supports 15-digit and 18-digit ID number resolution at the same time
    - **Main Method**:
        - **validate(String)**: Verify the validity of the ID number
        - **parseSex(String)**: Resolve the gender of the ID card holder
        - **parseBirthDate(String)**: Resolve the date of birth in the ID card


- **IdUtils**: ID generation tool class
    - **Function**:
        - Provide a variety of distributed ID generation solutions
        - Support UUID, MongoDB ObjectId, NanoId, snowflake algorithm, etc.
        - ID generation containing both standard and simplified formats
    - **Core Features**:
        - **UUID generation**:
            - Standard UUID (with hyphen) and simplified UUID (without hyphen)
            - High-performance UUID implementation, supports custom random sources
        - **ObjectId**:
            - ObjectId generation compatible with MongoDB
            - Composite ID based on timestamps, machine codes and counters
        - **NanoId**:
            - Provide URL-safe random string ID
            - Supports custom length configuration
        - **Snowflake Algorithm ID**:
            - Distributed ID based on Twitter Snowflake algorithm
            - 64-bit long integer, including timestamp, working machine ID and serial number
    - **Main Method**:
        - **randomUUID/simpleRandomUUID**: Generate standard/simplify UUID
        - **fastUUID/simpleFastUUID**: High performance standard/simplified UUID
        - **objectId**: Generate MongoDB-style ObjectId
        - **nanoId**: Generates NanoId of default or specified length
        - **snowflakeId**: Generate distributed ID based on snowflake algorithm


- **JsonUtils**: JSON processing tool class
    - **Function**:
        - JSON serialization and deserialization tools based on Gson implementation
        - Provides bidirectional conversion of Java objects, JSON strings, and JsonElement
        - Supports the conversion of collection types and JsonArray
    - **Core Features**:
        - **Preconfigured Gson instance**: Provides default configuration Gson instances, supporting null value
          serialization
        - **Type Adapter**: Built-in serialization/deserialization support for Date, LocalDate, LocalDateTime,
          BigDecimal and other types
        - **Generic support**: Completely support serialization and deserialization of generic types
        - **Null value safety**: All methods safely handle null values to avoid NullPointerException
        - **Custom Gson**: Supports operation using custom Gson instances
    - **Main Method Classification**:
        - **String operation**: fromString/toString series methods, handle the conversion of JSON strings and Java
          objects
        - **JsonElement operation**: fromJson/toJson series methods, handle the conversion of JsonElement and Java
          objects
        - **Array operation**: fromJsonArray/toJsonArray series methods, handle the conversion of JsonArray and Java
          collections
        - **GsonBuilder**: Provide createGsonBuilder method to create a preconfigured GsonBuilder


- **ReflectionUtils**: Reflection operation tool class
    - **Function**:
        - Inherit and extend the functionality of org.reflections.ReflectionUtils
        - Provide reflection-related operations such as field access, method processing, and class information
          acquisition
        - Supports secure access to private fields and methods
    - **Core Features**:
        - **Field operation**: Get/set object field values, support private field access
        - **Type information**: Get type information such as class name, generic type parameters, etc.
        - **Proxy Processing**: Identify and process CGLIB proxy classes
        - **Method Identification**: Determine common method types (equals/hashCode/toString, etc.)
        - **Access Control**: Provides access control for fields and methods
    - **Main Method Classification**:
        - **Field access**: getFieldValue/setFieldValue series method
        - **Type information**: getClassName/getClassGenericType series method
        - **Proxy Processing**: getUserClass/isCglibRenamedMethod and other methods
        - **Method judgment**: isEqualsMethod/isHashCodeMethod/isToStringMethod, etc.
        - **Access control**: isAccessible/makeAccessible series method


- **RegExUtils**: Regex Tool Class
    - **Function**:
        - Inherited and extended the RegExUtils functionality of Apache Commons Lang
        - Provides enhanced functions such as regular expression compilation, matching detection, pattern search, etc.
        - Supports flexible configuration of regular expression flag bits
    - **Core Features**:
        - **Mode Compilation**: Supports regular expression compilation method with multiple parameter combinations
        - **Start and end match**: Automatically add the start (^) and end ($) matching symbols
        - **Flat bit management**: Supports calculation of regular expression flag bits through enumeration combinations
        - **Match detection**: Provides exact match detection of strings and regular expressions
        - **Schema Lookup**: Find all substrings in a string that match regular expressions
    - **Main Method**:
        - **compile series**: Method for compiling a pattern object with multiple parameters
        - **matches series**: Check whether the string exactly matches the regular expression
        - **find series**: Find all matching substrings in a string and return to the list
        - **computeFlags**: Computes the combined value of regular expression flag bits


- **SerializationUtils**: Java object serialization tool class (copyed from Spring framework)
    - **Function**:
        - Provide static tool methods for serializing and deserializing Java objects
        - Implementation based on Java standard serialization mechanism
        - Code originated from SerializationUtils of Spring Framework
    - **Core Features**:
        - **Object Serialization**: Serialize Java objects into byte arrays
        - **Object cloning**: Deep cloning of objects is achieved through serialization and deserialization
        - **Null value processing**: Safe processing of null values
        - **Exception handling**: Provide friendly exception information and type conversion
    - **Main Method**:
        - **serialize(Object)**: Serialize an object into a byte array
        - **clone(T)**: Deep cloning of objects through serialization mechanism
    - **Safety Tips**:
        - It should be used with caution, refer to the Java programming language safe coding guide
        - Only for serialization and deserialization of trusted data


- **StringFormatUtils**: String Format Conversion Tool Class
    - **Function**:
        - Provides multiple naming format conversion methods
        - Supports mutual conversion between various programming naming specifications
        - Handle smart conversion of case and separator
    - **Core Features**:
        - **Camel nomenclature**: Supports conversion of small camelCase and large camel (PascalCase) formats
        - **Unscore nomenclature**: Support snake_case and SCREAMING_SNAKE_CASE format conversion
        - **Medium horizontal line nomenclature**: Supports kebab-case and SCREAMING-KEBAB-CASE format conversion
        - **Custom delimiter**: Supports format conversion using custom delimiters
        - **Null value security**: All methods safely process null values
    - **Main Method**:
        - **formatAsCamelCase**: Convert to small camel format (such as: userName)
        - **formatAsPascalCase**: Convert to large camel format (such as: UserName)
        - **formatAsSnakeCase**: Convert to underscore format (such as: user_name)
        - **formatAsScreamingSnakeCase**: Convert to full capital underscore format (such as: USER_NAME)
        - **formatAsKebabCase**: Convert to medium horizontal line format (such as: user-name)
        - **formatAsScreamingKebabCase**: Convert to full capital mid-line format (such as: USER-NAME)


- **StringUtils**: String tool class
    - **Function**:
        - Inherit and extend the StringUtils functionality of Apache Commons Lang
        - Provide enhanced methods such as character set conversion, collection element filtering, etc.
        - Supports non-empty element extraction of string collections and arrays
    - **Core Features**:
        - **Character set conversion**: Supports conversion of strings between different character sets
        - **Collection Filter**: Extract non-empty string elements from a collection or array
        - **Deduplication processing**: Supports obtaining unique and non-empty string elements
        - **Null value security**: All methods safely process null values
    - **Main Method**:
        - **convertCharset Series**: Convert strings between different character sets
        - **getNotBlankElements Series**: Get non-empty string elements in a collection or array
        - **getUniqueNotBlankElements Series**: Get unique and non-empty string elements in a collection or array


- **TreeUtils**: Tree structure construction tool class
    - **Function**:
        - Provides the ability to convert flat data into tree structures
        - Support single/multi-root tree construction
        - Suitable for tree-shaped data structure processing such as menus and directories
    - **Core Features**:
        - **Tree Transformation**: Convert linear collection data to hierarchy
        - **Flexible configuration**: Supports custom root node identification
        - **Node Processing**: Supports conversion processing of nodes during the construction process
        - **Generic Support**: Applicable to any node type that implements the TreeNode interface
    - **Main Method**:
        - **toTree(Collection, rootNodeKey)**: Basic tree structure construction method
      - **toTree(Collection, rootNodeKey, convertFunc)**: A tree structure construction method that supports node
        conversion


- **MoneyUtils**: money utilities class
    - **Functions**:
        - Amount formatting (support for thousands of separators, retain 2 decimal places)
        - Amount to Chinese uppercase (support to billion level, including negative number processing)
        - Support Double and BigDecimal type of amount processing.
        - Support for standardized display and upper case conversion of amounts

### 🔒 pangju-commons-crypto (secure encryption module)

Encryption tool module, implements data encryption based on jasypt.

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-crypto</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **CryptoConstants**: Crypto algorithm-related constant classes
    - **Function**:
        - Provides the standard algorithm name used in the encryption module
        - Define default configuration parameters and compliance check related constants
        - Standard configuration parameters containing multiple cryptographic algorithms
    - **Constant Classification**:
        - **Algorithm Name**:
            - RSA_ALGORITHM: RSA algorithm standard name
            - DIFFIE_HELLMAN_ALGORITHM: Diffie-Hellman key exchange algorithm standard name
            - DSA_ALGORITHM: Standard name of DSA digital signature algorithm
        - **Key length**:
            - RSA_DEFAULT_KEY_SIZE: RSA default key length (2048 bits)
            - RSA_KEY_SIZE_SET: Allowed set of RSA key lengths (1024/2048/4096 bits)
            - DIFFIE_HELLMAN_KEY_SIZE_SET: Allowed Diffie-Hellman key length set
            - DSA_KEY_SIZE_SET: Allowed DSA key length set
    - **Technical Features**:
        - Comply with Java cipher architecture (JCA) standard naming
        - Set the recommended key length according to industry security standards
        - Provide key length compliance verification support


- **RSAKey**: RSA key pair immutable container class
    - **Function**:
        - Encapsulate the public and private keys required for RSA algorithms
        - Provides multiple key generation and resolution methods
        - Ensure thread safety and immutability of key objects
    - **Core Features**:
        - **Immutable Design**: All fields are final, ensuring thread safety
        - **Multiple format support**: Supports random generation, key pair conversion, raw bytes and Base64 parsing
        - **Null value security**: Allow public or private keys to be null separately
        - **Standard compatibility**: Strictly follow X.509 and PKCS#8 standards
    - **Core Method**:
        - **Key Generation**:
            - random(): Generates a random RSA key pair of default length (2048 bits)
            - random(int): Generates a random RSA key pair of specified length (1024/2048/4096 bits)
        - **Key Conversion**:
            - fromKeyPair(): Build an RSAKey instance from an existing KeyPair
            - fromRawBytes(): Build RSAKey from raw byte array
            - fromBase64String(): Build RSAKey from Base64 encoded string
    - **Technical Features**:
        - Implementing immutable objects using Java Record
        - Provide comprehensive parameter verification
        - Automatically handle exception conversion
        - Supports multiple key formats and encodings


- **KeyPairUtils**: KeyPair Tool Class
    - **Function**:
        - Provides key pair generation, key encoding and key object conversion functions
        - Encapsulate key operations in Java security system
        - Supports key operations of multiple asymmetric encryption algorithms (RSA, DSA, etc.)
    - **Core Features**:
        - **Object Cache**: Cache KeyFactory and KeyPairGenerator instances to improve performance
        - **Multiple format support**: Supports multiple key formats such as raw bytes, Base64 encoded strings, etc.
        - **Standard compatibility**: Strictly follow the PKCS#8 and X.509 standards
        - **Thread Safety**: All methods are thread-safe static methods
    - **Core Method**:
        - **Key Generation**:
            - generateKeyPair(): generates key pairs for the specified algorithm (supports default parameters/specified
              length/custom random sources)
        - **Key resolution**:
            - getPrivateKeyFromPKCS8Base64String()/getPrivateKeyFromPKCS8RawBytes(): parse PKCS#8 format private key
            - getPublicKeyFromX509Base64String()/getPublicKeyFromX509RawBytes(): parse X.509 format public key
        - **Factory Get**:
            - getKeyFactory(): Get the key factory of the specified algorithm (with cache mechanism)
    - **Technical Features**:
        - Implement object caching using ConcurrentHashMap
        - Strict parameter verification and exception handling
        - Supports multiple key formats and encodings
        - Provide detailed documentation and usage examples


- **RSAByteDigester**: RSA digital signature processor
    - **Function**:
        - Implement digital signature function based on RSA asymmetric encryption algorithm
        - Provides a complete solution for signature generation and verification
        - Supports multiple JCA standard signature algorithms (SHA256withRSA, etc.)
    - **Core Features**:
        - **Signature generation**: Use private key to digitally sign data
        - **Signature Verification**: Use public key to verify the validity of the signature
        - **Algorithm extension**: Supports multiple signature algorithms (SHA256withRSA/SHA384withRSA, etc.)
        - **Thread Safety**: All key operations are controlled synchronously
        - **Delay initialization**: Initialize signature components on demand to improve resource utilization
    - **Construction method**:
        - Supports default configuration to create instances quickly
        - Supports custom key length (1024/2048/4096 bits)
        - Supports custom signature algorithms
        - Supports the use of pre-generated RSA key pairs
    - **Technical Features**:
        - Implement the ByteDigester interface and provide standardized operations
        - Automatically handle null values ​​and boundary conditions
        - Provide detailed exception information and types
        - Supports independent use of public and private keys


- **RSAStringDigester**: RSA String Signature Processor
    - **Function**:
        - Implement string signature function based on RSA algorithm
        - Provides signature generation and verification of string messages
        - Supports signature output and verification in multiple encoding formats
    - **Core Features**:
        - **String Signature**: Generate a digital signature for string messages
        - **Signature verification**: Verify the matching of string messages and signatures
        - **Multiple formats support**: Support Base64 and hexadecimal encoding formats
        - **Thread Safety**: All operations are controlled synchronously
    - **Encoding format**:
        - **Base64 encoding**: Use digest()/matches() method
        - **Hex encoding**: Use digestToHexString()/matchesFromHexString() method
    - **Technical Features**:
        - Based on RSAByteDigester implementation, reuse the underlying signature logic
        - Automatically process string encoding conversion (UTF-8)
        - Provide null value safe processing
        - Implement the StringDigester interface and provide standardized operations


- **RSATransformation**: RSA encryption conversion policy interface
    - **Function**:
        - Define encryption algorithm mode, fill method and block processing logic
        - Provide standardized conversion solution definitions
        - Supports multiple fill modes and chunking strategies
    - **Core Responsibilities**:
        - **Algorithm definition**: Provide standardized algorithm/mode/fill names
        - **Block calculation**: Calculate encryption/decryption block size according to key specifications
        - **Extended support**: Allow custom filling scheme implementation
    - **Main Method**:
        - getName(): Get the complete algorithm conversion scheme name
        - getEncryptBlockSize(): Calculate the size of public key encryption blocks
        - getDecryptBlockSize(): Calculate the private key decryption block size (default implementation)
    - **Built-in implementation**:
        - **RSAPKCS1PaddingTransformation**: PKCS#1 v1.5 filling solution implementation
        - **RSAOEAPWithSHA256Transformation**: OAEP-SHA256 filling solution implementation (recommended)
    - **Technical Features**:
        - Interface design supports policy mode
        - Provide default implementation to reduce duplicate code
        - Strictly follow the JCE standard naming specifications
        - Automatically process chunking size calculation


- **RSABinaryEncryptor**: RSA binary data encryption and decryptor
    - **Function**:
        - Implement binary data encryption/decryption function based on RSA algorithm
        - Provides segmented encryption/decryption capabilities to automatically process big data
        - Asymmetric encryption process that supports public key encryption/private key decryption
    - **Core Features**:
        - **Segmented encryption/decryption**: Automatically handle blocking operations of big data
        - **Multiple key support**: Support public key encryption/private key decryption
        - **Algorithm extension**: Supports multiple fill modes (such as OAEPWithSHA-256)
        - **Thread Safety**: All key operations are controlled synchronously
    - **Construction method**:
        - Supports default configuration to create instances quickly
        - Supports custom key length (1024/2048/4096 bits)
        - Supports custom encryption schemes
        - Supports the use of pre-generated RSA key pairs
    - **Technical Features**:
        - Implement the BinaryEncryptor interface and provide standardized operations
        - Delay initialization design to improve resource utilization
        - Automatically handle null values ​​and boundary conditions
        - Provide detailed exception information and types


- **RSATextEncryptor**: Text encryption decryptor based on RSA algorithm
    - **Function**:
        - Provides secure asymmetric text encryption and decryption capabilities
        - Implement the TextEncryptor interface, specializing in processing string data
        - Supports two output encoding formats: Base64 and hexadecimal
    - **Core Features**:
        - **Security Algorithm**: Adopt RSA asymmetric encryption, supports 2048/3072/4096-bit key strength
        - **Encoding supports **: Base64 encoding (network transmission) and hexadecimal encoding (debug log)
        - **Encryption solution**: Supports multiple fill modes such as PKCS#1 v1.5, OAEP, etc.
        - **Character encoding**: Force UTF-8 encoding to ensure cross-platform consistency
    - **Construction method**:
        - Supports default security configuration to quickly create instances
        - Supports custom key length and encryption schemes
        - Supports the use of pre-generated RSA key pairs
        - Support multiplexing of existing binary encryptor configurations
    - **Technical Features**:
        - Thread-safe design, suitable for concurrent environments
        - Delegate mode, the underlying dependency is RSABinaryEncryptor
        - Automatically handle null values ​​and boundary conditions
        - Provide detailed exception information and types


- **RSADecimalNumberEncryptor**: RSA algorithm high-precision floating-point number encryptor
    - **Function**:
        - Provides accurate encryption and decryption capabilities of BigDecimal type
        - Achieve floating-point lossless encryption by separating scales and scaleless values
        - Ensure that the original numerical accuracy can be completely restored after decryption
    - **Core Features**:
        - **Accuracy reserve***: Precision ensures the number of decimal places and accuracy of the original value
        - **Security Algorithm**: Adopt RSA asymmetric encryption algorithm, supporting 2048/3072/4096-bit keys
        - **Numerical integrity**: Encrypted BigDecimal still maintains mathematical operation characteristics
        - **Format compatibility**: Processing complement formats ensure cross-platform consistency
    - **Technical implementation**:
        - Encryption process: BigDecimal → Separate scale → Scale-free encryption → Reorganization
        - Decryption process: parse encrypted values ​​→ decrypt scaleless values ​​→ apply original scale
        - Maximum processing accuracy: Supports any scale of BigDecimal
    - **Construction method**:
        - Supports default financial-level security configuration to quickly create instances
        - Supports custom key length and encryption schemes
        - Supports the use of pre-generated RSA key pairs
        - Support multiplexing of existing binary encryptor configurations
    - **Application Scenarios**:
        - Financial system amount encryption (such as exchange rate and interest rate calculation)
        - Scientific computing data protection
        - Database sensitive floating-point field encryption
        - Security protocols that require precise decimal operations


- **RSAIntegerNumberEncryptor**: Large integer encryption decryptor based on RSA algorithm
    - **Function**:
        - Provides accurate BigInteger type numerical encryption capabilities
        - Mathematical properties of retaining original numerical values
        - Suitable for safe scenarios where precise numerical operations are required
    - **Core Features**:
        - **Precise encryption**: Maintain the numerical accuracy and symbol bits of BigInteger
        - **Security Algorithm**: Adopt RSA asymmetric encryption algorithm, supporting 2048/3072/4096-bit keys
        - **Coding Specification**: Use two's complement format to process values ​​to ensure cross-platform consistency
        - **Length ID**: Automatically add 4-byte length header, supports variable-length data decryption
    - **Technical implementation**:
        - Encryption process: BigInteger → complement byte array → RSA encryption → Add length header → New BigInteger
        - Decryption process: Encrypt BigInteger → Extract data → RSA decryption → Reconstruct the original BigInteger
        - Maximum encryption length: key length/8 - padding length - 4 byte header
    - **Construction method**:
        - Supports default security configuration to quickly create instances
        - Supports custom key length and encryption schemes
        - Supports the use of pre-generated RSA key pairs
        - Support multiplexing of existing binary encryptor configurations
    - **Application Scenarios**:
        - Financial transaction amount encryption
        - Large number operations in cryptography protocols
        - Database ID field encryption
        - Numerical encryption that requires precise recovery

### ✔️ pangju-commons-validation (check module)

Verification module, implemented based on jakarta.validation.

````xml
<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-validation</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **annotation**: Verification annotation
    - **Basic Verification**:
        - `@NotBlankElements`: Verify that the string elements in the collection are not blank
        - `@RegexElements`: Verify that the set element matches the specified regular expression
    - **Format verification**:
        - `@BankCard`: Bank card number format verification (supports mainstream banks)
        - `@ChineseName`: Chinese name verification (2-4 Chinese characters)
        - `@IdCard`: ID card number verification (supports 18-digit + verification code)
        - `@Filename`: File name format verification (support extension verification)
        - `@HexColor`: Hexadecimal color value verification
        - `@IP`: IPv4/IPv6 address format verification
        - `@Identifier`: Universal identifier verification (alphanumeric underscore)
        - `@Base64`: Base64 encoding format verification
        - `@Md5`: MD5 hash format verification
      - `@Xss`: Xss protection verification
    - **Business Verification**:
        - `@HttpMethod`: HTTP request method verification (GET/POST, etc.)
        - `@MimeType`: Media type format verification (compliant with IANA standards)
        - `@RequestPath`: HTTP request path format verification
        - `@EnumName`: Enumeration name validity verification
        - `@Number`: Number format verification (supports positive and negative/decimal)
        - `@PhoneNumber`: Phone number format verification (land phone/mobile phone)

### 🌏 pangju-commons-geo (geographic information module)

The geographic information module encapsulates coordinate conversion, analysis, judgment and other methods.

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-geo</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **CoordinateType**: Enumeration of geographic coordinate system types
    - **Function**:
        - Define and manage common geographic coordinate system standards
        - Provides conversion capabilities between coordinate systems
        - Encapsulate the conversion logic between different coordinate systems
    - **Supported coordinate systems**:
        - GCJ_02: National Bureau of Testing Coordinate System (Mars Coordinate System)
            - China's official map coordinate system
            - Nonlinear encryption based on WGS-84
            - Map services such as Gaode, Tencent, Baidu, etc. are adopted
            - There is a 50-500-meter offset from WGS-84
        - WGS_84: World Earth Coordinate System
            - International General GPS Coordinate System
            - Original coordinate system of GPS equipment
            - International map services such as Google Earth are adopted
    - **Core Method**:
        - toGCJ02(): Convert the current coordinate system coordinates to GCJ-02 coordinates
        - toWGS84(): Convert the current coordinate system coordinates to WGS-84 coordinates


- **GeoConstants**: GEO geographic information related constant class
    - **Function**:
        - Provide common constant definitions in geographic information processing
        - Contains latitude and longitude symbols, direction identifiers, and geographic boundary values
    - **Constant Classification**:
        - **Symbol Constant**:
            - RADIUS_CHAR: Degree symbol (°)
            - MINUTE_CHAR: Segment (')
            - SECONDS_CHAR: Second symbol (")
        - **Direction Sign**:
            - North_DIRECTION: Northern Identifier (N)
            - SOUTH_CHAR: Southern Identifier (S)
            - EAST_CHAR: Oriental Identifier (E)
            - WEST_CHAR: Western Identifier (W)
        - **Global Boundary Value**:
            - MIN_LATITUDE/MAX_LATITUDE: World minimum/maximum latitude value (-90°/90°)
            - MIN_LONGITUDE/MAX_LONGITUDE: World minimum/maximum longitude value (-180°/180°)
        - **China Boundary Value**:
            - CHINA_MIN_LATITUDE: The latitude of Zeng Mu's dark sand in the southernmost part of China (0.8293°)
            - CHINA_MAX_LATITUDE: The latitude of Mohe, the northernmost part of China (55.8271°)
            - CHINA_MIN_LONGITUDE: Longitude of the Pamir Plateau in the westernmost part of China (72.004°)
            - CHINA_MAX_LONGITUDE: Longitude at the intersection of Heilongjiang and Ussuri River at the easternmost end
              of China (137.8347°)


- **Coordinate**: Geographic coordinate model class
    - **Function**:
        - Represents a geographic coordinate point with high precision
        - Encapsulated coordinate verification, format conversion and position judgment functions
        - Use BigDecimal to ensure calculation accuracy
        - Suitable for Geographic Information System (GIS), Map Service and other scenarios
    - **Core Features**:
        - Immutability: Use Java record type to achieve thread safety
        - Accuracy guarantee: Use BigDecimal storage to avoid floating point errors
        - Automatic verification: Check the latitude and longitude range during construction
        - Format conversion: Supports multiple coordinate formats
    - **Construction method**:
        - Main construction method: receive latitude and longitude of BigDecimal type
        - Double precision construction method: receive double-type latitude and longitude
        - Degree minute and second format construction method: receive DMS format string
    - **Core Method**:
        - isOutOfChina(): determines whether the coordinates are outside China
        - toString(): Convert coordinates to standard minute-second representation


- **CoordinateUtils**: Geographic coordinate conversion tool class
    - **Function**:
        - Provides high-precision geographic coordinate conversion function
        - Support coordinate system conversion (WGS-84 and GCJ-02 conversion)
        - Provides time-to-second format and decimal system format to convert each other
        - Implement high-precision calculation based on BigDecimal
    - **Core Method**:
        - **Coordinate system conversion**:
            - WGS84ToGCJ02(): World Earth Coordinate System to Mars Coordinate System
            - GCJ02ToWGS84(): Mars coordinate system to world geodetic coordinate system
        - **Format conversion**:
            - toLatitudeDMS(): Decimal latitude rotation minute and second format
            - toLongitudeDms(): Decimal longitude rotation minute and second format
            - fromDMS(): To convert the decimal system to the minute and second format
        - **Internal implementation**:
            - computeGcj02Delta(): Calculate the offset of the GCJ-02 coordinate system
            - transformLongitude()/transformLatitude(): latitude and longitude offset transformation calculation
    - **Technical Features**:
        - High-precision math operation using BigDecimalMath
        - Calculation based on WGS84 ellipsoid parameters
        - GCJ02 Offset Algorithm Reference Public Implementation
        - Conversion accuracy up to ±50-500 meters

### 📁 pangju-commons-io (IO module)

IO module provides various operations such as file name, file, io stream, file type judgment, etc.

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-io</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **FileType**: File Type Enumeration Class
    - **Function**:
        - File classification system based on MIME type
        - Supports two matching modes:
            - Prefix matching: match a certain type of file through typePrefix (such as "image/" to match all images)
            - Exact match: match specific file types through type collections (such as various specific formats of
              compressed packages)
        - Predefined 7 common file types:
            - IMAGE: Image type (JPEG, PNG, GIF, WEBP, etc.)
            - TEXT: Text type (TXT, CSV, HTML, XML, etc.)
            - AUDIO: Audio type (MP3, WAV, AAC, OGG, etc.)
            - MODEL: 3D model type (STL, OBJ, FBX, etc.)
            - VIDEO: Video type (MP4, AVI, MKV, MOV, etc., including HLS streaming media)
            - COMPRESS: Compressed package type (TAR, GZIP, ZIP, RAR, 7Z, CAB, etc.)
            - DOCUMENT: Office document types (PDF, Excel, Word, PPT, etc.)


- **IOConstants**: IO related constant class
    - **Function**:
        - Centrally manage constant definitions related to IO operations
        - Provide thread-safe Tika singleton instance acquisition method
        - Define prefix constants of various MIME types
    - **Constant Classification**:
        - **MIME type prefix**:
            - IMAGE_MIME_TYPE_PREFIX: Image type prefix ("image/")
            - VIDEO_MIME_TYPE_PREFIX: Video type prefix ("video/")
            - AUDIO_MIME_TYPE_PREFIX: Audio type prefix ("audio/")
            - MODEL_MIME_TYPE_PREFIX: 3D model type prefix ("model/")
            - TEXT_MIME_TYPE_PREFIX: Text type prefix ("text/")
            - APPLICATION_MIME_TYPE_PREFIX: Application type prefix ("application/")
    - **Core Method**:
        - getDefaultTika(): Get thread-safe Apache Tika singleton instance
            - Using double verification lock to achieve thread safety
            - Delayed initialization saves resources
            - For file content type detection


- **FilenameUtils**: Filename and path processing tool class
    - **Function**:
        - Inherited and extended the FilenameUtils function of Apache Commons IO
        - Provide a MIME type detection system based on file extension
        - Supports multiple file types to judge (picture/text/video/audio/application, etc.)
        - Exactly distinguish directory paths from file paths
        - Supports multi-MIME type set matching verification
        - Provide file name reconstruction function (full name replacement, base name replacement, extension
          replacement)
    - **Core Method**:
        - **MIME type detection**:
            - getMimeType(): Get the MIME type of the file
            - isMimeType(): Exactly match MIME type
            - isAnyMimeType(): batch matching MIME types (supports arrays and collections)
        - **File type judgment**:
            - isImageType(): determines whether it is an image type
            - isTextType(): determines whether it is a text type
            - isVideoType(): determines whether it is a video type
            - isAudioType(): determines whether it is an audio type
            - isModelType(): determines whether it is a 3D model type
            - isApplicationType(): determines whether it is an application type
        - **Intelligent path recognition**:
            - isDirectoryPath(): determines whether it is a directory path
            - isFilePath(): determines whether it is a file path
        - **File name reconstruction**:
            - rename(): Completely replace filename (including name and extension)
            - replaceBaseName(): Replace the base name of the file (preserve the extension and path)
            - replaceExtension(): replace file extension


- **FileUtils**: Enhanced file operation tool class
    - **Function**:
        - Inherited and extended the FileUtils function of Apache Commons IO
        - Provide high-performance IO streaming operation and support large file processing
        - Complete file encryption and decryption system (AES/CBC and AES/CTR modes)
        - File metadata analysis and content type detection based on Apache Tika
        - Enhanced file deletion and renaming capabilities
    - **Core Method**:
        - **High performance IO stream**:
            - openUnsynchronizedBufferedInputStream(): Open asynchronous buffered input stream
            - openBufferedFileChannelInputStream(): Open buffered file channel input stream
            - openMemoryMappedFileInputStream(): Open MemoryMappedFileInputStream
        - **File encryption and decryption**:
            - encryptFile()/decryptFile(): AES/CBC mode file encryption and decryption
            - encryptFileByCtr()/decryptFileByCtr(): AES/CTR mode file encryption and decryption
        - **File Type Detection**:
            - getMimeType(): Get the real MIME type of the file
            - isImageType()/isTextType()/isVideoType(), etc.: Detect file types
            - isMimeType()/isAnyMimeType(): Exactly match file MIME type
        - **Metadata Analysis**:
            - parseMetaData(): parse file content metadata
        - **File Operation**:
            - forceDeleteIfExist(): Force delete a file or directory
            - rename()/replaceBaseName()/replaceExtension(): File renaming and modification
            - exist()/notExist()/existFile()/notExistFile(): File existence check


- **IOUtils**: Enhanced IO stream operation tool class
    - **Function**:
        - Inherited and extended the IOUtils functionality of Apache Commons IO
        - Provide AES encryption and decryption capabilities based on Apache Commons Crypto
        - Supports streaming processing in two encryption modes: CBC and CTR
        - Provides asynchronous buffered flow implementation to optimize single-thread performance
        - Intelligent buffer size calculation system
    - **Core Method**:
        - **Buffer Optimization**:
            - getBufferSize(): Intelligently calculate the optimal buffer size based on the data size
        - **Async flow operation**:
            - unsynchronizedBuffer(): Create an asynchronous buffered input stream
            - toUnsynchronizedByteArrayInputStream(): Create an asynchronous byte array input stream
            - toUnsynchronizedByteArrayOutputStream(): Create an asynchronous byte array output stream
        - **AES/CBC encryption and decryption**:
            - encrypt()/decrypt(): Basic AES/CBC mode encryption and decryption (default IV)
            - encrypt()/decrypt(): Extended AES/CBC mode encryption and decryption (custom IV)
        - **AES/CTR encryption and decryption**:
            - encryptByCtr()/decryptByCtr(): Basic AES/CTR mode encryption and decryption (default IV)
            - encryptByCtr()/decryptByCtr(): Extended AES/CTR mode encryption and decryption (custom IV)
        - **General encryption and decryption**:
            - encrypt()/decrypt(): General encryption method (supports custom keys and algorithm parameters)

### 🖼️ pangju-commons-image (image module)

Image module, which provides various operations such as image type detection, type acquisition, width and height
reading, exif direction reading, thumbnail generation, application filters, etc.

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-image</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **ImageConstants**: Image processing related constant class
    - **Function**:
        - Provide common constant definitions in image processing
        - Management system-supported image format information
        - Thread-safe lazy loading implementation
    - **Constant Classification**:
        - **Format support**:
            - NON_TRANSPARENT_IMAGE_FORMATS: Image format collections that do not support transparent channels
        - **System Capabilities**:
            - SUPPORT_READ_IMAGE_TYPES: A collection of MIME types supported by the system
            - SUPPORT_WRITE_IMAGE_TYPES: A collection of MIME types supported by the system
    - **Core Method**:
        - getSupportReadImageTypes(): Get the MIME type collection supported by the system
        - getSupportWriteImageTypes(): Get the MIME type collection supported by the system
    - **Technical Features**:
        - Thread-safe initialization using double check lock
        - Use volatile to ensure visibility
        - Returns immutable collection to ensure data security


- **ImageSize**: Image size model class
    - **Function**:
        - Indicates the actual display size of the image after direction correction
        - Encapsulation width and height two immutable properties
        - Provides a variety of dimension scaling calculation methods to maintain aspect ratio
    - **Core Features**:
        - Immutability: Use Java record type to achieve thread safety
        - Aspect Ratio Hold: All scaling operations maintain original proportions
        - Pixel protection: The minimum calculation result is 1 pixel
    - **Core Method**:
        - **Single Constraint Scaling**:
            - scaleByWidth(): Equi-radio scaling based on the target width
            - scaleByHeight(): Equi-radio scaling based on target height
        - **Double Constraint Scaling**:
            - scale(ImageSize): Equi-scale scaling based on target size objects
            - scale(int, int): equal-radio scaling based on the target width and height value


- **ImageUtils**: Image processing tool class
    - **Function**:
        - Provides comprehensive image processing capabilities
        - Image metadata processing (reading EXIF ​​information, direction correction, etc.)
        - Image format detection (supports common formats such as JPEG, PNG, GIF, etc.)
        - Image size processing (automatically process EXIF ​​direction information)
        - MIME type conversion (image format and MIME type conversion)
    - **Core Method**:
        - **MIME type handling**:
            - isSupportReadType()/isSupportWriteType(): Check whether the MIME type supports read/write
            - isSameType(): Determine whether two MIME types belong to the same image type
            - getMimeType(): Get the MIME type of the image (multiple overloaded forms)
        - **Size handling**:
            - getSize(): Get image size (multiple overloading forms, automatically process EXIF ​​direction)
        - **EXIF processing**:
            - getExifOrientation(): Get the EXIF ​​direction information of the image
    - **Technical Features**:
        - Thread Safety - No Shared State
        - High performance - Provides optimization options for large files
        - Automatically process EXIF ​​direction information
        - Supports multiple input sources (files, byte arrays, input streams)


- **ThumbnailUtils**: Thumbnail Generation Tool Class
    - **Function**:
        - Provides a variety of image scaling strategies and output methods
        - Support for forced scaling (no consideration of original aspect ratio)
        - Supports equal-ratio scaling (maintains original aspect ratio)
        - Supports multiple output targets (file, output stream, BufferedImage object)
    - **Core Method**:
        - **Forced Scaling**:
            - forceScale(): Scale directly to the specified size regardless of the original aspect ratio
        - **Equal Scaling**:
            - scaleByWidth(): Scale the image according to the width equally
            - scaleByHeight(): Scale the image according to height equality
            - scale(): Scale the image according to the target size equally
        - **Internal implementation**:
            - resample(): Use resampling algorithm to process image scaling
    - **Technical Features**:
        - Automatically process image format conversion (such as transparent channel processing when JPG to PNG)
        - A variety of resampling filter options are available (triangle filter is used by default)
        - Complete parameter checksum exception handling
        - Thread-safe implementation
    - **Typical Application**:
        - Image thumbnail generation
        - Image scaling
        - Image format conversion


- **ImageFilterUtils**: Image filter processing tool class
    - **Function**:
        - Provide professional image filter processing functions
        - Supports core operations such as grayscale, brightness/contrast adjustment, etc.
        - Supports multiple output formats and output methods
        - All operations are thread-safe
    - **Core Method**:
        - **Grayscale processing**:
            - grayscale(): Convert an image to a grayscale image (multiple overloaded forms)
        - **Brightness Adjustment**:
            - brightness(): Adjust image brightness (supports [-2.0, 2.0] range adjustment)
        - **Contrast adjustment**:
            - contrast(): Adjust image contrast (supports [-1.0, 1.0] range adjustment)
        - **General filter**:
            - filter(): Apply custom image filters (supports any ImageFilter implementation)
    - **Technical Features**:
        - Multi-format support - Support common image formats such as PNG/JPG/BMP
        - Lossless processing - No operations affect the original image
        - High performance - based on efficient image processing algorithm implementation
        - Automatic format processing - automatically select the best image type according to the output format

### 🖼️🧩 pangju-commons-imageio (JAI plug-in module)

JAI plug-in module, providing Javax Image IO plug-in for various image types

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-imageio</artifactId>
    <version>1.0.0</version>
</dependency>
````

### 📦 pangju-commons-compress (compress module)

Compression module, providing decompression and compression operations for zip and 7z format compression package files

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-compress</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **CompressConstants**: File compression related constant classes
    - **Function**:
        - Provides common constant definitions in compressed file processing
        - MIME types containing common compression formats
        - Define the path separator inside the compressed file
    - **Constant Classification**:
        - **MIME type**:
            - ZIP_MIME_TYPE: zip compressed file MIME type ("application/zip")
            - SEVEN_Z_MIME_TYPE: 7z compressed file MIME type ("application/x-7z-compressed")
        - **Path Processing**:
            - PATH_SEPARATOR: Compressed file path separator ("/")
    - **Technical Features**:
        - Uninstantly designed (protected constructor)
        - Provide standardized constant references


- **SevenZUtils**: 7z compression decompression tool class
    - **Function**:
        - based on`Apache Commons Compress`Implemented 7z format compression tool
        - Supports high-efficiency compression algorithms such as LZMA2
        - Provides compression, decompression and format detection functions
        - Supports compression operations of files, directories and collections
    - **Core Method**:
        - **Format Detection**:
            - is7z(): Check whether the file/byte array/input stream is in a valid 7z compression format
        - **Decompress**:
            - unCompress(): Unzip 7z file to the specified directory
        - **compression**:
            - compress(): compress files/directories/collections to 7z files
    - **Technical Features**:
        - High-efficiency compression - Supports 7z proprietary compression algorithms such as LZMA2 and BZip2,
          providing high compression rates
        - Recursive processing - Automatically traverse directory structure for compression, maintaining the original
          file structure
        - Format verification - Ensure the validity of 7z files through file magic number and Tika detection
        - Large file support - streaming processing reduces memory consumption and supports GB-level file processing
        - Thread Safety - All methods are static methods and can be safely used in multithreaded environments


- **ZipUtils**: ZIP compression and decompression tool class
    - **Function**:
        - Provided based on`Apache Commons Compress`Enhanced features
        - Realize efficient compression and decompression operations in ZIP format
        - Supports multiple input sources and output targets
        - Automatically handle recursive compression of directory structures
    - **Core Method**:
        - **Format Detection**:
            - isZip(): Check whether the file/byte array/input stream is in a valid ZIP format
        - **Decompress**:
            - unCompress(): Decompress ZIP file/byte array/input stream to the specified directory
        - **compression**:
            - compress(): compress files/directories/collections to ZIP files or output streams
    - **Technical Features**:
        - Multi-input source support - Supports multiple compression sources such as files, byte arrays, input streams,
          ZipFile objects, etc.
        - Recursive compression - Automatically handle recursive compression of directory structures to maintain
          original file hierarchy relationships
        - Format security verification - Ensure the validity of ZIP file format through Tika detection and magic number
          verification
        - Large file optimization - Using buffered channel streams to improve large file processing performance
        - Resource Management - Automatically manage file resources to prevent resource leakage

### 📑 pangju-commons-compress (POI module)

POI module, providing various operations on excel and word files

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-poi</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **PoiConstants**: POI-related constant class
    - **Function**:
        - Provides the commonly used MIME type constant definition in Office document processing
        - Supports major Office formats such as Word, Excel, PowerPoint
        - Distinguish between new and old versions of Office file formats
    - **Constant Classification**:
        - **Word Documents**:
            - DOCX_MIME_TYPE: Word 2007 and above document MIME type
            - DOC_MIME_TYPE: Word 97-2003 version document MIME type
        - **Excel Workbook**:
            - XLSX_MIME_TYPE: Excel 2007 and above workbook MIME type
            - XLS_MIME_TYPE: Excel 97-2003 version workbook MIME type
        - **PowerPoint Presentation**:
            - PPTX_MIME_TYPE: PowerPoint 2007 and above presentation MIME type
            - PPT_MIME_TYPE: PowerPoint 97-2003 version presentation MIME type
    - **Technical Features**:
        - Standardized MIME type definition
        - Easy to identify and verify file types


- **HWPFDocumentUtils**: DOC Document Tool Class
    - **Function**:
        - Provides operational support for Microsoft Word 97-2003 format (.doc) documents
        - Implement document format verification and content reading functions
        - Supports multiple input sources (files, byte arrays, input streams)
    - **Core Method**:
        - **Format Verification**:
            - isDoc(): Check whether the file/byte array/input stream is in DOC format
        - **Document Loading**:
            - getDocument(): Loading DOC document from file/byte array
    - **Technical Features**:
        - Implement DOC document processing based on Apache POI
        - File type detection using Tika
        - Thread-safe design
        - Automatic resource management to prevent resource leakage
        - Strict parameter verification
    - **Precautions**:
        - Only .doc format documents are supported
        - All methods are static methods


- **XWPFDocumentUtils**: DOCX Document Tool Class
    - **Function**:
        - Provides operational support for Microsoft Word 2007 and above format (.docx) documents
        - Implement document format verification and content reading functions
        - Supports multiple input sources (files, byte arrays, input streams)
    - **Core Method**:
        - **Format Verification**:
            - isDocx(): Check whether the file/byte array/input stream is in DOCX format
        - **Document Loading**:
            - getDocument(): Loading DOCX document from file/byte array
    - **Technical Features**:
        - Implement DOCX document processing based on Apache POI
        - File type detection using Tika
        - Thread-safe design
        - Automatic resource management to prevent resource leakage
        - Strict parameter verification
    - **Precautions**:
        - Only .docx format documents are supported
        - All methods are static methods


- **XWPFTemplateUtils**: DOCX template tool class
    - **Function**:
        - Provide operational support for DOCX template documents
        - Implement template compilation, label processing and data model construction
        - Implement template functions based on poi-tl
    - **Core Method**:
        - **Template Compilation**:
            - compile(): Compile DOCX template file or byte array
        - **Tag handling**:
            - getTagNames(): Get all tag names in the template
        - **Data Model Construction**:
            - getDataModel(): builds a template data model and maps rendered data with label names
    - **Technical Features**:
        - Supports custom template configuration
        - Automatically verify the validity of DOCX format
        - Provides intelligent mapping of labels and data
        - Strict parameter verification and exception handling
    - **Precautions**:
        - Only .docx format templates are supported
        - All methods are static methods
        - Need to rely on poi-tl library


- **WorkbookUtils**: Excel workbook tool class
    - **Function**:
        - Provides comprehensive operational support for Excel workbooks (.xls and .xlsx formats)
        - Implement workbook format verification and content reading
        - Supports cell data processing and row-column operations
        - Provides style setting functions
    - **Core Method**:
        - **Format Verification**:
            - isXls()/isXlsx()/isWorkbook(): Check whether the file/byte array/input stream is in Excel format
        - **Workbook Loading**:
            - getWorkbook(): Load Excel workbook from file/byte array/input stream
        - **Content acquisition**:
            - getSheets()/getRows()/getCells(): Get sheets/rows/cells in a workbook
            - getMergedRegionCell(): Get cells in the merged cell range
        - **Data Processing**:
            - getStringCellValue()/getNumericCellValue(): Get the string/numeric value of the cell
            - getStringFormulaCellValue()/getNumericFormulaCellValue(): Get the value of the formula cell
        - **Streaming**:
            - sheetStream()/rowStream()/cellStream(): Create a stream of worksheets/rows/cells
    - **Technical Features**:
        - Supports both HSSF (.xls) and XSSF (.xlsx) formats
        - File type detection using Tika
        - Provide streaming API to support parallel processing
        - Strict parameter verification and exception handling
        - Thread-safe design
    - **Precautions**:
        - All methods are static methods

### 📚 pangju-commons-compress (PDF module)

PDF module, providing various operations on PDF files

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-pdf</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **PdfConstants**: PDF-related constant class
    - **Function**:
        - Provide common constant definitions in PDF document processing
        - Contains standard MIME type definitions
    - **Constant Classification**:
        - **MIME type**:
            - PDF_MIME_TYPE: Standard MIME type for PDF documents (application/pdf)
    - **Technical Features**:
        - Standardized MIME type definition
        - Easy to identify and verify PDF file types


- **Bookmark**: PDF document bookmark model class
    - **Function**:
        - Represents bookmark nodes in PDF documents and supports hierarchical structures
        - Implement TreeNode interface and provide tree structure operation capabilities
        - Related PDF document page number, support page jump
    - **Core Attributes**:
        - **id**: Bookmark unique identifier, automatic generation of UUID
        - **parentId**: Parent bookmark ID, supports hierarchical relationships
        - **name**: Bookmark name, displayed in PDF document
        - **pageNumber**: Associated page number (1-based)
        - **children**: Collection of sub-bookmarks, maintaining hierarchical structure
    - **Technical Features**:
        - Automatically generate unique IDs, use the IdUtils tool class
        - Support the construction of parent-child hierarchical relationships
        - Automatic page number conversion (0-based to 1-based)
        - Implement TreeNode interface for easy tree structure operation
    - **Construction method**:
        - Supports direct creation of top-level bookmarks
        - Supports creating child bookmarks by specifying parent bookmark ID


- **PDDocumentUtils**: PDF document advanced operation tool class
    - **Function**:
        - Advanced PDF document processing tool based on Apache PDFBox 3.x package
        - Provides PDF document format verification, content reading and processing functions
        - Supports PDF and image interchange, page extraction, document merging and splitting
        - Implement document bookmark management
    - **Core Features**:
        - **Intelligent memory management**: Automatically select the optimal processing mode (memory/mixed/temporary
          files) according to file size
        - **Comprehensive I/O support**: Supports multiple data sources such as files, byte arrays, input streams, etc.
        - **Image processing**: Supports PDF page to image, and can specify scaling or DPI
        - **Bookmark operation**: Provides reading and building functions of bookmark tree structure
    - **Memory Management Policy**:
        - Small file (<50MB): Pure memory mode, best performance
        - Medium Files (50MB-500MB): Mixed Mode, Balancing Performance and Memory
        - Large file (>500MB): Temporary file mode, minimum memory usage
    - **Technical Features**:
        - Thread-safe design, all methods are stateless static methods
        - Strict parameter verification, use Validate for precondition check
        - Unified exception handling mechanism
        - Automatically preserve original document attributes and metadata
    - **Precautions**:
        - The caller is responsible for closing the returned PDDocument object
        - Pay attention to disk space and memory usage when processing large files

## Open Source Protocol

This project adopts the Apache License 2.0 open source protocol.