package dto

class MessageDTO(userId: String, id: String, title: String, body: String) {

  def getUserId: String = userId

  def getId: String = id


  def getTitle: String = title


  def getBody: String = body


  override def toString: String = "MessageDTO{" + "userId='" + userId + '\'' + ", id='" + id + '\'' + ", title='" + title + '\'' + ", body='" + body + '\'' + '}'
}
