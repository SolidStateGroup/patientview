Then /^the staff, patient, codes, links sections should be available$/ do
  page.should have_content('Home')
  page.should have_content('Groups')
  page.should have_content('Codes')
  page.should have_content('Staff')
  page.should have_content('Patients')
end

Given(/^the "(.*?)" section$/) do |section|
  visit 'http://diabetes-pv.dev.solidstategroup.com/#/'
  click_on section
end

When(/^a Specialty group "(.*?)" is created with the code "(.*?)"$/) do |name, code|
  click_on 'Create New'
  fill_in 'Code', :with => code
  fill_in 'Name', :with => name
  select('SPECIALTY', :from => 'group-groupType')
  within '.modal-footer' do
    click_on 'Create New'
  end
end

When(/^the "(.*?)" section is selected$/) do |section|
  click_on section
end

Then (/^the group "(.*?)" should be accessible/) do |name|
  fill_in '.input-block-level', :with => name
  within '.accordion-group' do
    page.should have_content('name')
  end
end