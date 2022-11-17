import { Component, OnInit } from '@angular/core';

import { Todo } from '../todo';
import { TodoService } from '../todo.service';

@Component({
  selector: 'app-todo-list',
  templateUrl: './todo-list.component.html',
  styleUrls: ['./todo-list.component.css']
})
export class TodoListComponent implements OnInit {

  todoList: Todo[] = [];

  constructor(private todoService: TodoService) { }

  ngOnInit(): void {
    //this.getTodoList();
    //TODO debug
    this.todoList = [
      { id: 1, category: 1, title: 'Test1', content: 'hogehoge', state: 0},
      { id: 2, category: 1, title: 'Test2', content: 'hogehoge', state: 0},
      { id: 3, category: 1, title: 'Test3', content: 'hogehoge', state: 0},
      { id: 4, category: 1, title: 'Test4', content: 'hogehoge', state: 0},
      { id: 5, category: 1, title: 'Test5', content: 'hogehoge', state: 0},
      { id: 6, category: 1, title: 'Test6', content: 'hogehoge', state: 0},
      { id: 7, category: 1, title: 'Test7', content: 'hogehoge', state: 0},
    ]
  }

  getTodoList(): void {
    this.todoService.getTodoList().subscribe(
      todoList => this.todoList = todoList
    )
  }

  /*
  add(name: string): void {
    name = name.trim();
    if (!name) { return; }
    this.heroService.addHero({ name } as Hero)
      .subscribe(hero => {
        this.heroes.push(hero);
      });
  }
  */

  delete(todo: Todo): void {
    this.todoList = this.todoList.filter(t => t !== todo);
    this.todoService.deleteTodo(todo.id).subscribe();
  }
}
