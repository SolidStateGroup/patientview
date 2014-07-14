Given /^the patient view homepage$/ do
  visit "http://diabetes-pv.dev.solidstategroup.com/#/"
end

When /^the "(.*?)" login credentials \("(.*?)","(.*?)"\) are submitted$/ do |user, username, password|
  @user = user

  click_on "Log In"
  fill_in "username", :with => username
  fill_in "password", :with => password
  within "#login-form" do
    click_on "Log In"
  end
end

