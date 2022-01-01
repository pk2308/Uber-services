Feature: Testing base functionality.
  Testing for system funciton.
  Scenario: Successful check all users infomation
    Given administrator has been authorized.
    When administrator click to view all users' infomation.
    Then return headcount should equals all user's amount.

  Scenario: Account user to view personal info
    Given acouunt user "user1" has already been authorized.
    When "user1" click MyInfo anchor.
    Then display personal info to "user1" 