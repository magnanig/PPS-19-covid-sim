package pps.covid_sim.view.viewUtil

import scala.swing.TextField

object Checkers {

  /**
   * Method that check if the inserted value is a number between 0 and 100
   * @param textField containing the value to check
   */
  def checkPercent(textField: TextField): Unit = {
    if (textField.text.forall(c => c.isDigit) && textField.text!="") {
      if (textField.text.toInt > 100) {textField.text = "100"}
      else if (textField.text.toInt < 0) {textField.text = "0"}
    } else {textField.text = "0"}
  }

  /**
   * Method that check if the inserted value is a number positive number
   * @param textField containing the value to check
   */
  def checkPositive(textField: TextField): Unit = {
    if (textField.text.forall(c => c.isDigit) && textField.text!="") {
      if (textField.text.toInt < 0) {textField.text = "0"}
    } else {textField.text = "0"}
  }

  /**
   * Method that check if the inserted value is a plausible day of a date
   * @param textField containing the value to check
   */
  def checkDay(textField: TextField): Unit = {
    if (textField.text.forall(c => c.isDigit) && textField.text!="") {
      if (textField.text.toInt < 0) {textField.text = "1"}
      if (textField.text.toInt >31) {textField.text = "0"}
    } else {textField.text = "1"}
  }

  /**
   * Method that check if the inserted value plausible month of a date
   * @param textField containing the value to check
   */
  def checkMonth(textField: TextField): Unit = {
    if (textField.text.forall(c => c.isDigit) && textField.text!="") {
      if (textField.text.toInt < 0) {textField.text = "1"}
      if (textField.text.toInt > 12) {textField.text = "12"}
    } else {textField.text = "1"}
  }

}
