require 'faker'
require 'json'
require 'pmap'

def gen(number, force_array = false, &block)
  max = if number.is_a?(Range)
        number.last < Float::INFINITY ? number.last : 3
      else
        number
      end
  min = number.is_a?(Range) ? number.first : number

  res = (rand((max - min) + 1) + min).times.map(&block).compact
  return if res.empty?
  #$stderr.puts "#{number}, #{res.inspect}" if max == 1
  max > 1 || force_array ? res : res.first
end

def gen_identifier(number = 1..1)
  tpls = [
    { use: 'official', label: 'BSN', system: 'urn:oid:2.16.840.1.113883.2.4.6.3', value: '123123' },
    { use: "official", label:"SSN", system:"urn:oid:2.16.840.1.113883.2.4.6.7", value:"123456789" }]

  gen(number) do
    tpls.sample.merge value: (1000000 + rand(1000000)).to_s, use: %w(usual  official  temp  secondary).sample
  end
end

def gen_boolean(number = 1..1)
  gen(number) { [true, false].sample }
end
alias :gen_active :gen_boolean

def gen_integer(number = 1..1)
  gen(number) { rand(100) }
end

def gen_uri(number = 1..1)
  gen(number) { Faker::Internet.url }
end

def gen_string(number = 1..1)
  gen(number) { Faker::Lorem.word }
end

def gen_text(number = 1..1)
  gen(number) { Faker::Lorem.paragraphs }
end

def gen_datetime(number = 1..1)
  datetime_min = Time.new(2000)
  datetime_max = Time.new(2010)

  gen(number) do
    Time.at((datetime_max.to_f - datetime_min.to_f) * rand + datetime_min.to_f)
  end
end
alias :gen_date :gen_datetime
alias :gen_dateTime :gen_datetime
alias :gen_instant :gen_datetime

def gen_period(number = 1..1)
  gen(number) do
    res = {}
    res[:start] = gen(0..1) { gen_datetime } # Starting time with inclusive boundary
    res[:end]   = gen(0..1) { gen_datetime } # End time with inclusive boundary, if not ongoing

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end

def gen_codeable_concept(number = 1..1)
  gen(number) do
    res = {}
    res[:coding] = gen_coding(0..Float::INFINITY) # Coding Code defined by a terminology system
    res[:text] = gen(0..1) { Faker::Lorem.sentence } # Plain text representation of the concept

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end
alias :gen_communication :gen_codeable_concept
alias :gen_maritalStatus :gen_codeable_concept

def gen_coding(number = 1..1)
  gen(number) do
    res = {}
    res[:system] = gen_uri(0..1) # Identity of the terminology system
    res[:version] = gen_string(0..1) # Version of the system - if relevant
    res[:code] = gen_code(0..1) # Symbol in syntax defined by the system
    res[:display] = gen_string(0..1) # Representation defined by the system
    res[:primary] = gen_boolean(0..1) # If this code was chosen directly by the user
    res[:valueSet] = gen_valueSet(0..1) # Set this coding was chosen from

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end

def gen_valueset(number = 1..1)
  gen(number, true) do
    res = {}
    res[:identifier] = gen_string(0..1) # Logical id to reference this value set
    res[:version] = gen_string(0..1) # Logical id for this version of the value set
    res[:name] = gen_string(1..1) # Informal name for this value set
    res[:publisher] = gen_string(0..1) # Name of the publisher (Organization or individual)
    res[:telecom] = gen_contact(0..Float::INFINITY) # Contact information of the publisher</telecom>
    res[:description] = gen_text(1..1) # Human language description of the value set
    res[:copyright] = gen_string(0..1) # About the value set or its content
    res[:status] = gen_code(1..1, restrictions: %w(draft active retired))
    res[:experimental] = gen_boolean(0..1) # If for testing purposes, not real usage
    res[:extensible] = gen_boolean(0..1) # Whether this is intended to be used with an extensible binding
    res[:date] = gen_date(0..1) # Date for given status
    res[:define] = gen(0..1) do # When value set defines its own codes
      res[:system] = gen_uri(1..1) # URI to identify the code system
      res[:version] = gen_string(0..1) # Version of this system
      res[:caseSensitive] = gen_boolean(0..1) # If code comparison is case sensitive
      res[:concept] = gen(0..Float::INFINITY) do # Concepts in the code system
        res[:code] = gen_code(1..1) # Code that identifies concept
        res[:abstract] = gen_boolean(0..1) # If this code is not for use as a real concept
        res[:display] = gen_string(0..1) # Text to Display to the user
        res[:definition] = gen_text(0..1) # Formal Definition
        # <concept><!-- 0..* Content as for ValueSet.define.concept Child Concepts (is-a / contains) </concept>
      end
    end
    res[:compose] = gen(0..1) do # When value set includes codes from elsewhere
      res[:import] = gen_uri{0..Float::INFINITY} # Import the contents of another value set
      res[:include] = gen(0..Float::INFINITY) do # Include one or more codes from a code system
        res[:system] = gen_uri(1..1) # The system the codes come from
        res[:version] = gen_string(0..1) # Specific version of the code system referred to
        res[:code] = gen_code(0..Float::INFINITY) # Code or concept from system
        res[:filter] = gen(0..Float::INFINITY) do # Select codes/concepts by their properties (including relationships)
          res[:property] = gen_code(1..1) # A property defined by the code system
          res[:op] = gen_code(1..1, restrictions: %w(is-a is-not-a regex in not in))
          res[:value] = gen_code(1..1) # Code from the system, or regex criteria
        end
      end
      # <exclude><!-- ?? 0..Float::INFINITY Content as for ValueSet.compose.include Explicitly exclude codes </exclude>
    end
    res[:expansion] = gen(0..1) do # When value set is an expansion
      res[:identifier] = gen_identifier(0..1) # Identifier Uniquely identifies this expansion </identifier>
      res[:timestamp] = gen_instant(1..1) # Time valueset expansion happened
      res[:contains] = gen(0..Float::INFINITY) do # Codes in the value set
        res[:system] = gen_uri(0..1) # System value for the code
        res[:code] = gen_code(0..1) # Code - if blank, this is not a choosable code
        res[:display] = gen_string(0..1) # User display for the concept
        # <contains><!-- 0..Float::INFINITY Content as for ValueSet.expansion.contains Codes contained in this concept </contains>
      end
    end

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end
alias :gen_valueSet :gen_valueset

def gen_category(number = 1..1)
  gen(3) do
    { scheme: Faker::Internet.url('http://hl7.org/fhir/tag/'), term: Faker::Internet.url, label: Faker::Lorem.sentence }
  end
end

def gen_HumanName(number = 1..1)
  gen(number) do
    {
      use: %w(usual  official  temp  nickname  anonymous  old  maiden).sample,
      text: Faker::Name.name,
      family: gen(2) { Faker::Name.last_name },
      given: gen(2) { Faker::Name.first_name },
      prefix: gen(1, true) { Faker::Name.prefix }
    }
  end
end
alias :gen_name :gen_HumanName

def gen_telecom(number = 1..1)
  gen(number) do
    [
      {
        system: %w(phone fax).sample,
        value: Faker::PhoneNumber.cell_phone,
        use: %w(home  work  temp  old  mobile).sample
      },
      {
        system: 'email',
        value: Faker::Internet.email,
        use: %w(home  work  temp  old  mobile).sample
      }
    ].sample
  end
end

def gen_gender(number)
  gen(number) do
    {
      coding: [{ system: 'http://hl7.org/fhir/v3/AdministrativeGender', code: %w(M F).sample, display: %w(Male Female).sample }] # bug
    }
  end
end

def gen_birthDate(number = 1..1)
  gen(number) { Time.at(rand * Time.now.to_i) }
end

def gen_deceasedBoolean(number = 1..1)
  rand % 100 == 0
end

def gen_address(number = 2)
  gen(number) do
    res = { use: %w(home work temp old).sample,
      line: [Faker::Address.street_address],
      city: Faker::Address.city,
      zip: Faker::Address.zip,
      state: Faker::Address.state
    }
    res[:text] = [res[:line].join("\n"), res[:state], res[:zip]].join(", ")
    res
  end
end

def gen_contact(number = 1..1)
  gen(number) do
    res = {}

    res[:system] = gen_code(0..1, restrictions: %w(phone fax email url))
    res[:value] = gen(0..1) { Faker::Lorem.sentence } # The actual contact details
    res[:use] = gen_code(0..1, restrictions: %w(home work temp old mobile)) # purpose of this address
    res[:period] = gen_period(0..1) # Period Time period when the contact was/is in use

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end

def gen_code(number = 1, opts = {})
  restrictions = opts[:restrictions]
  if !restrictions.is_a?(Array)
    restrictions = [restrictions]
  end
  if restrictions.empty?
    restrictions = Faker::Lorem.words
  end

  gen(number) do
    restrictions.sample
  end
end

def gen_attachment(number = 1..1)
  gen(number) do
    res = {}
    res[:contentType] = gen_code(1..1) # Mime type of the content, with charset etc.
    res[:language] = gen_code(0..1) # Human language of the content (BCP-47)
    # <data value="[base64Binary]"/><!-- 0..1 Data inline, base64ed -->
    res[:url] = gen_uri(0..1) # Uri where the data can be found
    res[:size] = gen_integer(0..1) # Number of bytes of content (if url provided)
    # <hash value="[base64Binary]"/><!-- 0..1 Hash of the data (sha-1, base64ed ) -->
    res[:title] = gen_string(0..1) # Label to display in place of the data

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end
alias :gen_photo :gen_attachment

def gen_location(number = 1..1)
  gen(number) do
    res = {}
    res[:identifier] = gen_identifier(0..1) # Unique code or number identifying the location to its users
    res[:name] = gen(0..1) { Faker::Lorem.word } # Name of the location as used by humans
    res[:description] = gen(0..1) { Faker::Lorem.paragraphs } # Description of the Location, which helps in finding or referencing the place
    res[:type] = gen_codeable_concept(0..1) # Indicates the type of function performed at the location
    res[:telecom] = gen_contact(0..Float::INFINITY) # Contact details of the location
    res[:address] = gen_address(0..1) # Physical location
    res[:physicalType] = gen_codeable_concept(0..1) # Physical form of the location
    res[:position] = gen_position(0..1) # The absolute geographic location
    # SystemStackError: stack level too deep
    # res[:managingOrganization] = gen_organization(0..1) # The organization that is responsible for the provisioning and upkeep of the location
    res[:status] = gen_code(0..1, restrictions: %w(active suspended inactive))
    # SystemStackError: stack level too deep
    # res[:partOf] = gen_location(0..1) # Another Location which this Location is physically part of
    res[:mode] = gen_code(0..1, restrictions: %w(instance kind))

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end

def gen_position(number = 1..1)
  gen(number) do
    res = {}
    res[:longitude] = gen(1..1) { Faker::Address.longitude } # Longitude as expressed in KML
    res[:latitude] = gen(1..1) { Faker::Address.latitude } # Latitude as expressed in KML
    res[:altitude] = gen(0..1) { rand * 100 } # Altitude as expressed in KML

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end

def gen_organization(number = 1..1)
  gen(number) do
    res = {}
    res[:identifier] = gen_identifier(0..Float::INFINITY) # Identifier Identifies this organization  across multiple systems
    res[:name] = gen(0..1) { Faker::Company.name } # Name used for the organization
    res[:type] = gen_codeable_concept(0..1) # Kind of organization
    res[:telecom] = gen_contact(0..Float::INFINITY) # Contact A contact detail for the organization
    res[:address] = gen_address(0..Float::INFINITY) # Address An address for the organization </address>
    res[:partOf] = gen_organization(0..1) # The organization of which this organization forms a part
    res[:contact] = gen(0..Float::INFINITY) do # Contact for the organization for a certain purpose
      contact = {}
      contact[:purpose] = gen_codeable_concept(0..1) # The type of contact
      contact[:name] = gen_HumanName(0..1) # A name associated with the contact
      contact[:telecom] = gen_contact(0..Float::INFINITY) # Contact details (telephone, email, etc)  for a contact
      contact[:address] = gen_address(0..1) # Visiting or postal addresses for the contact
      contact[:gender] = gen_codeable_concept(0..1) # Gender for administrative purposes
      contact.delete_if { |_, v| !v }
      contact unless contact.empty?
    end
     res[:location] = gen_location(0..Float::INFINITY) # Location(s) the organization uses to provide services
     res[:active] = gen_active(0..1) # Whether the organization's record is still in active use

    res.delete_if { |_, v| !v }
    res unless res.empty?
  end
end
alias :gen_managingOrganization :gen_organization
alias :gen_careProvider :gen_organization

class Pager
  def initialize(number, ppm = true)
    @counter = 0
    @point = ppm ? number / 1000 : number / 100
    @line = @point * 20
  end

  def show
    @counter += 1
    if (@counter % @line).zero?
      $stderr.puts "  #{@counter}"
    elsif (@counter % @point).zero?
      $stderr.print '.'
    end
  end
end

def gen_patient(number)
  #pager = Pager.new(number)
  number.times.pmap do
    #pager.show
    res = { resourceType: 'Patient' }

    {
      identifier:           0..Float::INFINITY,
      category:             0..1, #wtf?
      name:                 0..Float::INFINITY,
      telecom:              0..Float::INFINITY,
      gender:               0..1,
      birthDate:            0..1,
      deceasedBoolean:      0..1,
      address:              0..Float::INFINITY,
      maritalStatus:        0..1,
      #photo:                0..Float::INFINITY,
      contact:              0..Float::INFINITY,
      communication:        0..Float::INFINITY,
      careProvider:         0..Float::INFINITY,
      managingOrganization: 0..1,
      active:               0..1
    }.each do |name, number|
      res[name] = send("gen_#{name}", number)
    end

    res.delete_if { |_, v| !v }
    unless res.empty?
      "SELECT fhir.insert_resource($DATA$#{JSON.pretty_generate(res)}$DATA$::json);"
    end
  end
end

gen_patient(ARGV[0].to_i).peach do |pt|
  puts pt
end
#puts JSON.pretty_generate(gen_patient(1000))

