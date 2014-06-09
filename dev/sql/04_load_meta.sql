\set fhir `cat $FHIRBASE_HOME/fhir/test/profiles-resources.xml`
\set datatypes `cat $FHIRBASE_HOME/fhir/test/fhir-base.xsd`

CREATE or REPLACE
FUNCTION xattr(pth varchar, x xml) returns varchar
  as $$
  BEGIN
    return  unnest(xpath(pth, x, ARRAY[ARRAY['fh', 'http://hl7.org/fhir']])) limit 1;
  END
$$ language plpgsql;

-- HACK: see http://joelonsql.com/2013/05/13/xml-madness/
-- problems with namespaces
CREATE OR REPLACE
FUNCTION xspath(pth varchar, x xml) returns xml[]
  as $$
  BEGIN
    return  xpath('/xml' || pth, xml('<xml xmlns:xs="xs">' || x || '</xml>'), ARRAY[ARRAY['xs','xs']]);
  END
$$ language plpgsql IMMUTABLE;

CREATE OR REPLACE
FUNCTION xsattr(pth varchar, x xml) returns varchar
  as $$
  BEGIN
    return  unnest(xspath( pth,x)) limit 1;
  END
$$ language plpgsql IMMUTABLE;


CREATE OR REPLACE
FUNCTION fpath(pth varchar, x xml) returns xml[]
  as $$
  BEGIN
    return xpath(pth, x, ARRAY[ARRAY['fh', 'http://hl7.org/fhir']]);
  END
$$ language plpgsql IMMUTABLE;

create OR replace
function xarrattr(pth varchar, x xml) returns varchar[]
  as $$
  BEGIN
    RETURN array(select unnest(fpath(pth, x))::varchar);
  END
$$ language plpgsql;

INSERT INTO meta.datatypes (version, type)
(
  select
    '0.12' as version,
    xsattr('/xs:simpleType/@name', st) as type
    FROM (
    SELECT unnest(xpath('/xs:schema/xs:simpleType', :'datatypes',
       ARRAY[ARRAY['xs', 'http://www.w3.org/2001/XMLSchema']])) st
  ) simple_types
  UNION
  select
    '0.12' as version,
    xsattr('/xs:complexType/@name', st) as type
    FROM (
    SELECT unnest(xpath('/xs:schema/xs:complexType', :'datatypes',
       ARRAY[ARRAY['xs', 'http://www.w3.org/2001/XMLSchema']])) st
  ) simple_types
);

INSERT INTO meta.datatype_enums (version, datatype, value)
SELECT
 '0.12' as version,
 datatype,
 xsattr('/xs:enumeration/@value', enum) as value
FROM
  (select
      xsattr('/xs:simpleType/@name', st) as datatype,
      unnest(xspath('/xs:simpleType/xs:restriction/xs:enumeration', st)) as enum
      FROM (SELECT unnest(xpath('/xs:schema/xs:simpleType', :'datatypes',
             ARRAY[ARRAY['xs', 'http://www.w3.org/2001/XMLSchema']])) st
      ) n1
  ) n2;


INSERT INTO meta.datatype_elements
(version, datatype, name, type, min_occurs, max_occurs)
SELECT
  '0.12' as version,
  datatype,
  coalesce(
    xsattr('/xs:element/@name', el),
    (string_to_array(xsattr('/xs:element/@ref', el),':'))[2]
  ) as name,
  coalesce(
    xsattr('/xs:element/@type', el),
    'text'
  ) as type,
  xsattr('/xs:element/@minOccurs', el) as min_occurs,
  xsattr('/xs:element/@maxOccurs', el) as max_occurs
FROM (
  SELECT
    xsattr('/xs:complexType/@name', st) as datatype,
    unnest(xspath('/xs:complexType/xs:complexContent/xs:extension/xs:sequence/xs:element', st)) as el
    FROM (
    SELECT unnest(xpath('/xs:schema/xs:complexType', :'datatypes',
       ARRAY[ARRAY['xs', 'http://www.w3.org/2001/XMLSchema']])) st
  ) n1
) n2;

INSERT INTO meta.resource_elements
 (version, path, min, max, type)
select
    '0.12' as version,
    regexp_split_to_array(xattr('./path/@value', el), '\.') as path,
    xattr('./definition/min/@value', el) as min,
    xattr('./definition/max/@value', el) as max,
    xarrattr('./definition/type/code/@value', el) as type
  FROM (
    SELECT unnest(fpath('//fh:structure/fh:element', :'fhir')) as el
  ) els
;
